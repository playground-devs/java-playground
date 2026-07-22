package com.portfolio.rebalance;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ExcelInputReader {

    public static class ParsedInput {
        public final List<Holding> holdings;

        public ParsedInput(List<Holding> holdings) {
            this.holdings = holdings;
        }
    }

    public ParsedInput read(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new IOException("Input file not found: " + path);
        }
        try (FileInputStream fis = new FileInputStream(path.toFile());
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getNumberOfSheets() > 0 ? wb.getSheetAt(0) : null;
            if (sheet == null) throw new IOException("Workbook has no sheets");

            Iterator<Row> it = sheet.iterator();
            if (!it.hasNext()) throw new IOException("Sheet is empty");

            // Find the real header row (skip intro rows)
            Map<String, Integer> colIdx = null;
            while (it.hasNext()) {
                Row candidate = it.next();
                try {
                    colIdx = mapHeaders(candidate);
                    break;
                } catch (IOException ignored) {
                    // not a header row; keep scanning
                }
            }
            if (colIdx == null) throw new IOException("Could not locate a header row with required columns");

            List<Holding> holdings = new ArrayList<>();
            while (it.hasNext()) {
                Row r = it.next();
                if (isEmptyRow(r)) continue;
                String symbol = getString(r, colIdx, "symbol");
                if (symbol == null || symbol.trim().isEmpty()) continue;
                if ("total".equalsIgnoreCase(symbol.trim())) continue;
                Double qty = getNumeric(r, colIdx, "quantity");
                Double price = getNumeric(r, colIdx, "price");
                Double target = getNumeric(r, colIdx, "target");

                if (qty == null) qty = 0.0;
                if (price == null) price = 0.0;
                if (target == null) target = 0.0;

                // Normalize symbol, support cash row
                if (isCash(symbol)) {
                    // Interpret quantity as cash amount; price 1; target as provided
                    holdings.add(new Holding("CASH", qty, 1.0, target));
                } else {
                    holdings.add(new Holding(symbol.trim(), qty, price, target));
                }
            }
            return new ParsedInput(holdings);
        }
    }

    private static boolean isEmptyRow(Row r) {
        if (r == null) return true;
        boolean allBlank = true;
        for (int i = r.getFirstCellNum(); i < r.getLastCellNum(); i++) {
            Cell c = r.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (c != null && c.getCellType() != CellType.BLANK) {
                allBlank = false;
                break;
            }
        }
        return allBlank;
    }

    private static Map<String, Integer> mapHeaders(Row header) throws IOException {
        Map<String, Integer> map = new HashMap<>();
        for (int i = header.getFirstCellNum(); i < header.getLastCellNum(); i++) {
            Cell c = header.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (c == null) continue;
            String key = normalizeHeader(c.toString());
            if (key == null || key.isEmpty()) continue;
            if (isSymbolHeader(key)) map.putIfAbsent("symbol", i);
            if (isQuantityHeader(key)) map.putIfAbsent("quantity", i);
            if (isPriceHeader(key)) map.putIfAbsent("price", i);
            if (isTargetHeader(key)) map.putIfAbsent("target", i);
        }
        if (!map.containsKey("symbol")) throw new IOException("Missing a symbol/ticker column in header");
        if (!map.containsKey("quantity")) throw new IOException("Missing a quantity column in header");
        if (!map.containsKey("price")) throw new IOException("Missing a price column in header");
        if (!map.containsKey("target")) throw new IOException("Missing a target% column in header");
        return map;
    }

    private static String normalizeHeader(String s) {
        return s == null ? null : s.trim().toLowerCase().replaceAll("[^a-z0-9]+", "");
    }

    private static boolean isSymbolHeader(String k) {
        return k.equals("symbol") || k.equals("ticker") || k.equals("tickersymbol") || k.equals("asset") || k.equals("security") || k.equals("name");
    }

    private static boolean isQuantityHeader(String k) {
        return k.equals("quantity") || k.equals("qty") || k.equals("shares") || k.equals("units") || k.equals("currentquantity") || k.equals("currentqty") || k.equals("currentunits") || k.equals("position");
    }

    private static boolean isPriceHeader(String k) {
        return k.equals("price") || k.equals("currentprice") || k.equals("px") || k.equals("marketprice") || k.equals("lastprice") || k.equals("pricepershare") || k.equals("closeprice");
    }

    private static boolean isTargetHeader(String k) {
        return k.equals("target") || k.equals("targetpct") || k.equals("targetpercent") || k.equals("targetweight") || k.equals("target%")
                || k.equals("model") || k.equals("modelpercent") || k.equals("model%") || k.equals("modelpct") || k.equals("targetallocation");
    }

    private static String getString(Row r, Map<String, Integer> idx, String key) {
        Integer i = idx.get(key);
        if (i == null) return null;
        Cell c = r.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (c == null) return null;
        if (c.getCellType() == CellType.STRING) return c.getStringCellValue();
        if (c.getCellType() == CellType.NUMERIC) return String.valueOf(c.getNumericCellValue());
        return c.toString();
    }

    private static Double getNumeric(Row r, Map<String, Integer> idx, String key) {
        Integer i = idx.get(key);
        if (i == null) return null;
        Cell c = r.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (c == null) return null;
        if (c.getCellType() == CellType.NUMERIC) return c.getNumericCellValue();
        if (c.getCellType() == CellType.STRING) {
            try { return Double.parseDouble(c.getStringCellValue().replace("%","")); } catch (Exception ignored) {}
        }
        try { return Double.parseDouble(c.toString().replace("%","")); } catch (Exception e) { return null; }
    }

    private static boolean isCash(String symbol) {
        if (symbol == null) return false;
        String t = symbol.trim().toUpperCase();
        return t.equals("CASH") || t.equals("USD");
    }
}
