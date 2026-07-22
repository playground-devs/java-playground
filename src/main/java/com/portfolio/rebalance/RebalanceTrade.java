package com.portfolio.rebalance;

public class RebalanceTrade {
    public enum Action { BUY, SELL, HOLD }

    private final String symbol;
    private final Action action;
    private final long shares; // integer shares
    private final double price;

    public RebalanceTrade(String symbol, Action action, long shares, double price) {
        this.symbol = symbol;
        this.action = action;
        this.shares = shares;
        this.price = price;
    }

    public String getSymbol() {
        return symbol;
    }

    public Action getAction() {
        return action;
    }

    public long getShares() {
        return shares;
    }

    public double getPrice() {
        return price;
    }

    public double getNotional() {
        return shares * price * (action == Action.SELL ? 1 : -1);
    }
}

