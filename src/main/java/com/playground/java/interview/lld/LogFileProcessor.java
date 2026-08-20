package com.playground.java.interview.lld;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * PATTERN: Low-Level Design / OOP (Streaming Line Processor + Strategy for parsing)
 * PRIORITY: P1 - High Priority
 * PROBLEM STATEMENT: Process a (potentially huge) log file line-by-line to count log levels
 * and extract ERROR lines, without ever loading the whole file into memory.
 *
 * ================= PROBLEM / REQUIREMENTS =================
 * - Log lines have the format: "<TIMESTAMP> <LEVEL> <MESSAGE>" e.g. "2026-08-20T10:00:01 ERROR Disk full".
 * - Supported levels: INFO, WARN, ERROR (unknown/malformed levels should be tracked separately, not crash).
 * - Must count number of occurrences of each log level across the whole file.
 * - Must extract/collect all lines classified as ERROR (for later alerting/reporting).
 * - Must process the file line-by-line (streaming) so a multi-GB file does not need to fit in RAM.
 * - Must be reusable for any actual file on disk (not just the in-memory demo data).
 * - Should report a summary (counts per level + malformed line count) at the end of a run.
 *
 * ================= SIMPLE APPROACH =================
 * A single method that does everything: reads the entire file into a List<String> (e.g. via
 * Files.readAllLines), loops over the list with simple if/else string checks to detect the level,
 * increments counters using local variables, and appends matches to an ArrayList<String> for errors.
 * No abstraction for "where lines come from" or "how a line is parsed" - everything is inline.
 *
 * ================= WHY IT'S NOT ENOUGH =================
 * - Files.readAllLines loads the ENTIRE file into heap memory as a List<String> before processing
 *   a single line - a 10GB log file needs ~10GB+ of heap, which will OOM most JVMs.
 * - Parsing logic (level detection, message extraction) is tangled with I/O and aggregation logic -
 *   violates Single Responsibility, hard to unit test parsing in isolation.
 * - Hard-coded if/else for levels means adding a new level (e.g. DEBUG, FATAL) requires touching
 *   the core loop instead of extending a data structure - violates Open/Closed Principle.
 * - No abstraction over "source of lines" - swapping file input for a network stream or test data
 *   means duplicating the whole method.
 *
 * ================= OPTIMIZED DESIGN =================
 * - LogLevel (enum): INFO, WARN, ERROR, UNKNOWN. Centralizes valid levels; adding a level = one enum
 *   constant, no logic changes elsewhere.
 * - LogEntry (static nested class): immutable value object holding timestamp, level, message, raw line.
 * - LineParser (interface) + DefaultLineParser (implementation): Strategy pattern for turning a raw
 *   String line into a LogEntry. Swappable if the log format changes (e.g. JSON logs) without touching
 *   the processing engine.
 * - LogLineSource (functional interface, Supplier<BufferedReader>): abstracts "where the lines come
 *   from" - a real file (Files.newBufferedReader), a test in-memory reader, or any Reader-backed
 *   source. The engine only depends on this abstraction, not on java.io.File directly.
 * - ProcessingResult (static nested class): aggregates counts-per-level (EnumMap<LogLevel, Long>),
 *   the list of ERROR-level entries, and a count of malformed lines. Single object returned by a run.
 * - LogFileProcessor (top-level, orchestrator): given a LogLineSource and a LineParser, opens a
 *   BufferedReader, reads with readLine() in a loop (constant memory per line), parses each line via
 *   the strategy, updates the ProcessingResult incrementally, and closes the reader (try-with-resources).
 *   This is effectively a Streaming/Pipeline pattern: read -> parse -> aggregate, one line at a time.
 *
 * ================= WHY THIS DESIGN =================
 * - Single Responsibility: parsing (LineParser), aggregation (ProcessingResult), and I/O orchestration
 *   (LogFileProcessor.process) are separate and independently testable.
 * - Open/Closed: new log levels are added to the enum; new line formats are added via a new LineParser
 *   implementation; neither requires editing the streaming engine.
 * - Dependency Inversion: the engine depends on the LogLineSource and LineParser abstractions, not on
 *   concrete file I/O or a hard-coded format, so it works identically against a real file, a network
 *   socket reader, or the in-memory demo data used in main().
 * - Memory efficiency is a first-class design goal: BufferedReader.readLine() yields one line at a
 *   time and is immediately eligible for GC after being parsed, giving O(1) memory relative to file size
 *   (aside from the ERROR list, which is bounded by the number of errors, not total lines).
 *
 * ================= EDGE CASES =================
 * - Empty file / empty line source: readLine() returns null immediately, result has all-zero counts.
 * - Blank lines: parser treats them as malformed (LogLevel.UNKNOWN) rather than throwing.
 * - Malformed line (missing level token, unrecognized level word): counted as UNKNOWN, does not abort
 *   the whole run - one bad line should not take down processing of a multi-GB file.
 * - Extremely large file: only one line is held in memory at a time by design (streaming).
 * - Concurrent processing of multiple files/sources: each call to process() opens/closes its own
 *   BufferedReader and returns its own ProcessingResult - no shared mutable state, so multiple threads
 *   can safely call process() on different sources concurrently without external synchronization.
 * - IOException while reading (e.g. disk error mid-file): propagated to the caller after ensuring the
 *   reader is closed (try-with-resources), rather than silently swallowed.
 *
 * ================= COMPLEXITY =================
 * // Time Complexity: O(n) where n = number of lines in the file (each line parsed once, O(1) work per line).
 * // Space Complexity: O(1) additional memory for the streaming loop itself, plus O(e) for the collected
 * // ERROR lines (e = number of ERROR entries) and O(1) for the fixed-size level-count map.
 *
 * ================= INTERVIEW FOLLOW-UPS =================
 * - How would you process the file using multiple threads for a faster wall-clock time, given that
 *   BufferedReader.readLine() itself is not safely shareable across threads without synchronization?
 *   (e.g. split file into byte-range chunks by seeking to line boundaries, or use one thread per file
 *   in a multi-file batch, then merge ProcessingResult objects.)
 * - How would you make ProcessingResult mergeable/thread-safe if multiple worker threads each produced
 *   partial results that need to be combined (e.g. AtomicLong counters or a merge() method with
 *   synchronized aggregation)?
 * - How would you extend this to support a new log level like DEBUG or FATAL without breaking existing
 *   callers? (Add enum constant + update DefaultLineParser's recognized tokens; EnumMap auto-adjusts.)
 * - How would you handle multi-line log entries (e.g. stack traces following an ERROR line)?
 * - How would you bound memory if the ERROR list itself could be huge (millions of errors) - e.g.
 *   write matches to an output file/stream incrementally instead of collecting them all in a List?
 * - How would you support processing a live-tailed file (like `tail -f`) instead of a finite file?
 * - What's the tradeoff between Files.readAllLines (simple, but O(n) memory) and streaming with
 *   BufferedReader/Files.lines (constant memory, but requires careful resource closing)?
 * - How would you unit test LineParser in isolation from real file I/O?
 */
public class LogFileProcessor {

    /** Supported log levels; UNKNOWN absorbs malformed/unrecognized lines instead of failing. */
    public enum LogLevel {
        INFO, WARN, ERROR, UNKNOWN
    }

    /** Immutable parsed representation of a single log line. */
    public static final class LogEntry {
        private final String timestamp;
        private final LogLevel level;
        private final String message;
        private final String rawLine;

        public LogEntry(String timestamp, LogLevel level, String message, String rawLine) {
            this.timestamp = timestamp;
            this.level = level;
            this.message = message;
            this.rawLine = rawLine;
        }

        public LogLevel getLevel() {
            return level;
        }

        public String getRawLine() {
            return rawLine;
        }

        @Override
        public String toString() {
            return "[" + timestamp + "] " + level + " - " + message;
        }
    }

    /** Strategy interface: converts one raw line of text into a LogEntry. */
    public interface LineParser {
        LogEntry parse(String rawLine);
    }

    /**
     * Default parser for the format: "<TIMESTAMP> <LEVEL> <MESSAGE>", space-delimited,
     * e.g. "2026-08-20T10:00:01 ERROR Disk full on /var".
     */
    public static final class DefaultLineParser implements LineParser {
        @Override
        public LogEntry parse(String rawLine) {
            if (rawLine == null || rawLine.trim().isEmpty()) {
                return new LogEntry("", LogLevel.UNKNOWN, "", rawLine == null ? "" : rawLine);
            }
            // Split into at most 3 parts: timestamp, level, remaining message.
            String[] parts = rawLine.trim().split("\\s+", 3);
            if (parts.length < 3) {
                return new LogEntry("", LogLevel.UNKNOWN, rawLine, rawLine);
            }
            LogLevel level;
            try {
                level = LogLevel.valueOf(parts[1]);
            } catch (IllegalArgumentException ex) {
                level = LogLevel.UNKNOWN;
            }
            return new LogEntry(parts[0], level, parts[2], rawLine);
        }
    }

    /**
     * Abstracts "where lines come from" so the engine can process a real file, a socket,
     * or in-memory test data identically. Caller is responsible for the returned reader's lifecycle
     * via try-with-resources inside process().
     */
    public interface LogLineSource extends Supplier<BufferedReader> {
    }

    /** Aggregated outcome of processing one log source. */
    public static final class ProcessingResult {
        private final Map<LogLevel, Long> countsByLevel = new EnumMap<>(LogLevel.class);
        private final List<LogEntry> errorEntries = new ArrayList<>();
        private long malformedLineCount = 0L;

        private void recordEntry(LogEntry entry) {
            countsByLevel.merge(entry.getLevel(), 1L, Long::sum);
            if (entry.getLevel() == LogLevel.ERROR) {
                errorEntries.add(entry);
            }
            if (entry.getLevel() == LogLevel.UNKNOWN) {
                malformedLineCount++;
            }
        }

        public long countFor(LogLevel level) {
            return countsByLevel.getOrDefault(level, 0L);
        }

        public List<LogEntry> getErrorEntries() {
            return errorEntries;
        }

        public long getMalformedLineCount() {
            return malformedLineCount;
        }

        public void printSummary() {
            System.out.println("Summary: INFO=" + countFor(LogLevel.INFO)
                    + ", WARN=" + countFor(LogLevel.WARN)
                    + ", ERROR=" + countFor(LogLevel.ERROR)
                    + ", MALFORMED=" + malformedLineCount);
        }
    }

    private final LineParser lineParser;

    public LogFileProcessor(LineParser lineParser) {
        this.lineParser = lineParser;
    }

    /**
     * Streams lines one at a time from the given source, parses each, and aggregates into a
     * ProcessingResult. Only one line is ever held in memory at once - this is the same code path
     * that would run against a real multi-GB file on disk via Files.newBufferedReader(path).
     */
    public ProcessingResult process(LogLineSource source) throws IOException {
        ProcessingResult result = new ProcessingResult();
        // try-with-resources guarantees the reader is closed even if parsing throws mid-file.
        try (BufferedReader reader = source.get()) {
            String line;
            // readLine() pulls exactly one line into memory at a time (streaming, not bulk load).
            while ((line = reader.readLine()) != null) {
                LogEntry entry = lineParser.parse(line);
                result.recordEntry(entry);
                // 'entry' and 'line' become eligible for GC on the next iteration - no accumulation
                // of raw lines, unlike Files.readAllLines() which would hold every line at once.
            }
        }
        return result;
    }

    /**
     * Wraps an in-memory list of lines as a LogLineSource, purely to simulate file content for the
     * demo below. A real deployment would instead do:
     *   LogLineSource fileSource = () -> {
     *       try { return Files.newBufferedReader(Paths.get("/var/log/app.log")); }
     *       catch (IOException e) { throw new UncheckedIOException(e); }
     *   };
     * which streams the real file line-by-line with the exact same process() method above - the
     * engine code does not change between the demo and a real 10GB file.
     */
    private static LogLineSource fromInMemoryLines(List<String> lines) {
        String joined = String.join(System.lineSeparator(), lines);
        return () -> new BufferedReader(new StringReader(joined));
    }

    public static void main(String[] args) throws IOException {
        // Simulated file content - in production this list does not exist; lines are read
        // directly from disk one at a time via BufferedReader.readLine().
        List<String> sampleLogLines = new ArrayList<>();
        sampleLogLines.add("2026-08-20T09:00:00 INFO Server started on port 8080");
        sampleLogLines.add("2026-08-20T09:00:05 INFO User login: alice");
        sampleLogLines.add("2026-08-20T09:01:12 WARN Connection pool at 80% capacity");
        sampleLogLines.add("2026-08-20T09:02:47 ERROR Failed to connect to database");
        sampleLogLines.add("");
        sampleLogLines.add("2026-08-20T09:03:00 INFO User login: bob");
        sampleLogLines.add("2026-08-20T09:04:33 ERROR NullPointerException in OrderService");
        sampleLogLines.add("garbage line with no valid level");
        sampleLogLines.add("2026-08-20T09:05:10 WARN Disk usage above threshold");

        LogFileProcessor processor = new LogFileProcessor(new DefaultLineParser());
        LogLineSource source = fromInMemoryLines(sampleLogLines);

        // Operation 1: stream-process the simulated file line-by-line.
        ProcessingResult result = processor.process(source);

        // Operation 2: print aggregate counts per level (+ malformed lines).
        result.printSummary();
        // Expected: Summary: INFO=3, WARN=2, ERROR=2, MALFORMED=2

        // Operation 3: individual level lookups.
        System.out.println("ERROR count: " + result.countFor(LogLevel.ERROR));
        // Expected: ERROR count: 2

        // Operation 4: extract and print all ERROR lines for alerting/reporting.
        System.out.println("Extracted ERROR entries:");
        for (LogEntry error : result.getErrorEntries()) {
            System.out.println("  " + error);
        }
        // Expected:
        //   [2026-08-20T09:02:47] ERROR - Failed to connect to database
        //   [2026-08-20T09:04:33] ERROR - NullPointerException in OrderService

        // Operation 5: process an empty source to confirm streaming handles the empty-file edge case.
        ProcessingResult emptyResult = processor.process(fromInMemoryLines(new ArrayList<>()));
        emptyResult.printSummary();
        // Expected: Summary: INFO=0, WARN=0, ERROR=0, MALFORMED=0
    }
}
