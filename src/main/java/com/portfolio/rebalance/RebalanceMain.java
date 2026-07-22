package com.portfolio.rebalance;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.List;

public class RebalanceMain {
    public static void main(String[] args) throws Exception {
        String defaultInput = "/Users/gramaraju/Desktop/Desktop-old-mac/2025/Calculations-Rebalance_a_portfolio.xlsx";
        Path input = Paths.get(args != null && args.length > 0 ? args[0] : defaultInput);
        Path output = Paths.get(args != null && args.length > 1 ? args[1] : "/Users/gramaraju/Documents/new_workspace/java-eight/build/rebalance-trades.csv");

        System.out.println("Reading input: " + input);
        ExcelInputReader reader = new ExcelInputReader();
        ExcelInputReader.ParsedInput parsed = reader.read(input);

        RebalanceCalculator calc = new RebalanceCalculator();
        RebalanceCalculator.Params params = new RebalanceCalculator.Params(false, 1.0); // ignore trades < $1
        RebalanceResult result = calc.rebalance(parsed.holdings, params);

        System.out.println(summary(result));
        writeCsv(output, result.getTrades());
        System.out.println("Trades written to: " + output);
    }

    private static String summary(RebalanceResult r) {
        DecimalFormat df2 = new DecimalFormat("#,##0.00");
        StringBuilder sb = new StringBuilder();
        sb.append("Starting Value: $").append(df2.format(r.getStartingValue())).append('\n');
        sb.append("Ending   Value: $").append(df2.format(r.getEndingValue())).append('\n');
        sb.append("Ending     Cash: $").append(df2.format(r.getEndingCash())).append('\n');
        sb.append("Trades:").append('\n');
        for (RebalanceTrade t : r.getTrades()) {
            sb.append("  ").append(t.getAction()).append(" ")
              .append(t.getShares()).append(" ")
              .append(t.getSymbol()).append(" @ $")
              .append(df2.format(t.getPrice())).append('\n');
        }
        return sb.toString();
    }

    private static void writeCsv(Path output, List<RebalanceTrade> trades) throws IOException {
        Files.createDirectories(output.getParent());
        try (BufferedWriter bw = Files.newBufferedWriter(output)) {
            bw.write("Symbol,Action,Shares,Price,Notional\n");
            DecimalFormat df2 = new DecimalFormat("0.00");
            for (RebalanceTrade t : trades) {
                double notional = t.getShares() * t.getPrice();
                bw.write(t.getSymbol());
                bw.write(',');
                bw.write(t.getAction().name());
                bw.write(',');
                bw.write(Long.toString(t.getShares()));
                bw.write(',');
                bw.write(df2.format(t.getPrice()));
                bw.write(',');
                bw.write(df2.format(notional));
                bw.write('\n');
            }
        }
    }
}

