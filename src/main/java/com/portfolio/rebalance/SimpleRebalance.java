package com.portfolio.rebalance;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Minimal, single-file rebalancer.
 * Expected Excel headers (first sheet): Symbol, Quantity, Price, Target%
 * A row with Symbol=CASH uses Quantity as cash amount and Price ignored.
 */
public class SimpleRebalance {

    static class RowData {
        String symbol;
        double qty;
        double price;
        double target; // can be 0..1 or 0..100
    }

    public static void main(String[] args) throws Exception {
        String defaultInput = "/Users/gramaraju/Desktop/2025/Calculations-Rebalance_a_portfolio.xlsx";
        Path input = Paths.get(args != null && args.length > 0 ? args[0] : defaultInput);

        List<RowData> rows = readRows(input);
        runRebalance(rows);
    }

    private static List<RowData> readRows(Path path) throws Exception {
        List<RowData> out = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(path.toFile());
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sh = wb.getSheetAt(0);
            Iterator<Row> it = sh.iterator();
            if (!it.hasNext()) throw new IllegalStateException("Empty sheet");

            // find header row (skip intro rows)
            Map<String,Integer> idx = null;
            Row header = null;
            while (it.hasNext()) {
                Row cand = it.next();
                try {
                    idx = headerMap(cand);
                    header = cand;
                    break;
                } catch (IllegalStateException e) {
                    // not a header row; keep scanning
                }
            }
            if (idx == null) throw new IllegalStateException("Could not locate a header row with required columns");

            while (it.hasNext()) {
                Row r = it.next();
                if (isBlank(r)) continue;
                RowData rd = new RowData();
                rd.symbol = str(r, idx.get("symbol"));
                if (rd.symbol == null || rd.symbol.isEmpty()) continue;
                if (rd.symbol.trim().equalsIgnoreCase("Total")) continue;
                rd.qty = num(r, idx.get("quantity"), 0);
                rd.price = num(r, idx.get("price"), 0);
                rd.target = num(r, idx.get("target"), 0);
                out.add(rd);
            }
        }
        return out;
    }

    private static void runRebalance(List<RowData> rows) {
        DecimalFormat df2 = new DecimalFormat("#,##0.00");
        double cash = rows.stream().filter(r -> eqCash(r.symbol)).mapToDouble(r -> r.qty).sum();
        List<RowData> assets = new ArrayList<>();
        for (RowData r : rows) if (!eqCash(r.symbol)) assets.add(r);

        double startAssets = assets.stream().mapToDouble(r -> r.qty * r.price).sum();
        double startTotal = startAssets + cash;

        // Normalize targets (if they sum to > 1.5, treat as percents)
        double targetSum = rows.stream().mapToDouble(r -> r.target).sum();
        boolean isPercent = targetSum > 1.5;
        if (isPercent) {
            for (RowData r : rows) r.target = r.target / 100.0;
        }
        // Exclude cash from targets; renormalize across assets
        double assetTargetSum = assets.stream().mapToDouble(r -> r.target).sum();
        if (assetTargetSum == 0) assetTargetSum = 1.0; // avoid div by zero

        // Compute desired share deltas rounded toward zero
        Map<String, Long> shareDelta = new LinkedHashMap<>();
        for (RowData a : assets) {
            double weight = a.target / assetTargetSum; // renormalized
            double tgtVal = startTotal * weight;
            double curVal = a.qty * a.price;
            double dVal = tgtVal - curVal;
            long dShares = roundTowardZero(dVal / nz(a.price));
            // cap sells to current shares
            if (dShares < 0) dShares = Math.max(dShares, -Math.round(a.qty));
            shareDelta.put(a.symbol, dShares);
        }

        // Compute cash impact and adjust buys to fit cash
        double cashAfter = cash;
        for (RowData a : assets) {
            long d = shareDelta.get(a.symbol);
            cashAfter -= d * a.price; // buy reduces cash; sell increases
        }
        if (cashAfter < 0) {
            // Reduce buys one share at a time, more expensive first
            assets.sort((x,y) -> Double.compare(y.price, x.price));
            int i = 0;
            while (cashAfter < 0 && i < 100000) { // guard
                boolean adjusted = false;
                for (RowData a : assets) {
                    long d = shareDelta.get(a.symbol);
                    if (d > 0) {
                        shareDelta.put(a.symbol, d - 1);
                        cashAfter += a.price;
                        adjusted = true;
                        if (cashAfter >= 0) break;
                    }
                }
                if (!adjusted) break; // nothing to reduce
                i++;
            }
        }

        System.out.println("Starting Value: $" + df2.format(startTotal));
        System.out.println("Ending   Cash: $" + df2.format(cashAfter));
        System.out.println("Trades:");
        for (RowData a : assets) {
            long d = shareDelta.get(a.symbol);
            if (d == 0) continue;
            String act = d > 0 ? "BUY" : "SELL";
            System.out.println("  " + act + " " + Math.abs(d) + " " + a.symbol + " @ $" + df2.format(a.price));
        }
    }

    // Helpers
    private static boolean eqCash(String s) {
        if (s == null) return false;
        String t = s.trim().toUpperCase();
        return t.equals("CASH") || t.equals("USD");
    }
    private static long roundTowardZero(double v) { return v >= 0 ? (long)Math.floor(v) : (long)Math.ceil(v); }
    private static double nz(double v) { return v == 0.0 ? 1e-9 : v; }

    private static Map<String,Integer> headerMap(Row header) {
        Map<String,Integer> m = new HashMap<>();
        List<String> seen = new ArrayList<>();
        for (int i = header.getFirstCellNum(); i < header.getLastCellNum(); i++) {
            Cell c = header.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (c == null) continue;
            String raw = c.toString();
            String k = norm(raw);
            seen.add(raw);
            // symbol/ticker variations
            if (k.equals("symbol") || k.equals("ticker") || k.equals("tickersymbol") || k.equals("asset") || k.equals("security") || k.equals("name")) m.putIfAbsent("symbol", i);
            // quantity variations
            if (k.equals("quantity") || k.equals("qty") || k.equals("shares") || k.equals("units") || k.equals("currentquantity") || k.equals("currentunits") || k.equals("currentqty") || k.equals("position")) m.putIfAbsent("quantity", i);
            // price variations
            if (k.equals("price") || k.equals("currentprice") || k.equals("px") || k.equals("marketprice") || k.equals("lastprice") || k.equals("pricepershare") || k.equals("closeprice")) m.putIfAbsent("price", i);
            // target variations
            if (k.equals("target") || k.equals("targetpercent") || k.equals("targetweight") || k.equals("targetpct") || k.equals("target%") || k.equals("targetallocation") || k.equals("targetweight%") || k.equals("targetweightpercent")
                    || k.equals("model") || k.equals("modelpercent") || k.equals("model%") || k.equals("modelpct")) m.putIfAbsent("target", i);
        }
        if (!m.containsKey("symbol") || !m.containsKey("quantity") || !m.containsKey("price") || !m.containsKey("target")) {
            System.err.println("Header row found: " + String.join(" | ", seen));
            throw new IllegalStateException("Missing required headers: Symbol, Quantity, Price, Target% (case-insensitive; supports Ticker, Units, Market Price, Target Allocation)");
        }
        return m;
    }

    private static boolean isBlank(Row r) {
        if (r == null) return true;
        for (int i = r.getFirstCellNum(); i < r.getLastCellNum(); i++) {
            Cell c = r.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (c != null && c.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }

    private static String norm(String s) { return s == null ? "" : s.trim().toLowerCase().replaceAll("[^a-z0-9]+"," ").replace(" ",""); }

    private static String str(Row r, Integer i) {
        if (i == null) return null;
        Cell c = r.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (c == null) return null;
        if (c.getCellType() == CellType.STRING) return c.getStringCellValue();
        if (c.getCellType() == CellType.NUMERIC) return String.valueOf(c.getNumericCellValue());
        return c.toString();
    }

    private static double num(Row r, Integer i, double def) {
        if (i == null) return def;
        Cell c = r.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (c == null) return def;
        if (c.getCellType() == CellType.NUMERIC) return c.getNumericCellValue();
        try { return Double.parseDouble(c.toString().replace("%","")); } catch (Exception e) { return def; }
    }
}
