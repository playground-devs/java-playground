package com.portfolio.rebalance;

public class Holding {
    private final String symbol;
    private final double quantity;
    private final double price;
    private final double targetWeight; // 0..1

    public Holding(String symbol, double quantity, double price, double targetWeight) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.targetWeight = targetWeight;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public double getTargetWeight() {
        return targetWeight;
    }

    public double currentValue() {
        return quantity * price;
    }

    public boolean isCash() {
        return "CASH".equalsIgnoreCase(symbol);
    }
}

