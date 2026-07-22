package com.portfolio.rebalance;

import java.util.Collections;
import java.util.List;

public class RebalanceResult {
    private final List<RebalanceTrade> trades;
    private final double startingValue;
    private final double endingValue;
    private final double endingCash;

    public RebalanceResult(List<RebalanceTrade> trades, double startingValue, double endingValue, double endingCash) {
        this.trades = trades;
        this.startingValue = startingValue;
        this.endingValue = endingValue;
        this.endingCash = endingCash;
    }

    public List<RebalanceTrade> getTrades() {
        return Collections.unmodifiableList(trades);
    }

    public double getStartingValue() {
        return startingValue;
    }

    public double getEndingValue() {
        return endingValue;
    }

    public double getEndingCash() {
        return endingCash;
    }
}

