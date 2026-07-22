package com.portfolio.rebalance;

import java.util.*;
import java.util.stream.Collectors;

public class RebalanceCalculator {

    public static class Params {
        public final boolean allowCashTarget; // if true, CASH weight participates in targets
        public final double minTradeNotional; // ignore trades below this notional

        public Params(boolean allowCashTarget, double minTradeNotional) {
            this.allowCashTarget = allowCashTarget;
            this.minTradeNotional = minTradeNotional;
        }
    }

    public RebalanceResult rebalance(List<Holding> holdings, Params params) {
        Objects.requireNonNull(holdings, "holdings");
        if (holdings.isEmpty()) {
            return new RebalanceResult(Collections.emptyList(), 0, 0, 0);
        }

        // Separate cash and assets
        double cash = holdings.stream().filter(Holding::isCash).mapToDouble(Holding::currentValue).sum();
        List<Holding> assets = holdings.stream().filter(h -> !h.isCash()).collect(Collectors.toList());

        double startingAssetValue = assets.stream().mapToDouble(Holding::currentValue).sum();
        double startingTotal = startingAssetValue + cash;

        // Target weights sanity: if provided as percents 0..100, normalize to 0..1
        double weightSum = holdings.stream().mapToDouble(Holding::getTargetWeight).sum();
        if (weightSum > 1.5) { // likely given as 0..100
            assets = assets.stream()
                    .map(h -> new Holding(h.getSymbol(), h.getQuantity(), h.getPrice(), h.getTargetWeight() / 100.0))
                    .collect(Collectors.toList());
            double cashWeight = holdings.stream().filter(Holding::isCash).mapToDouble(Holding::getTargetWeight).sum() / 100.0;
            cash = cash; // unchanged
            weightSum = cashWeight + assets.stream().mapToDouble(Holding::getTargetWeight).sum();
        }

        // If cash target not allowed, renormalize weights excluding CASH
        double assetWeightSum = assets.stream().mapToDouble(Holding::getTargetWeight).sum();
        if (!params.allowCashTarget && assetWeightSum > 0) {
            final double scale = 1.0 / assetWeightSum;
            assets = assets.stream()
                    .map(h -> new Holding(h.getSymbol(), h.getQuantity(), h.getPrice(), h.getTargetWeight() * scale))
                    .collect(Collectors.toList());
        }

        // Compute desired target values and share deltas (rounded toward zero)
        double totalForTargets = startingTotal; // we rebalance against current total
        List<AssetPlan> plans = new ArrayList<>();
        for (Holding h : assets) {
            double targetValue = totalForTargets * h.getTargetWeight();
            double deltaValue = targetValue - h.currentValue();
            long sharesDelta = roundTowardZero(deltaValue / safePrice(h.getPrice()));
            // Prevent shorting beyond current position
            if (sharesDelta < 0) {
                long maxSell = -Math.round(h.getQuantity());
                if (sharesDelta < maxSell) sharesDelta = maxSell;
            }
            plans.add(new AssetPlan(h, targetValue, deltaValue, sharesDelta));
        }

        // Compute cash impact and adjust buys if overspending
        double cashAfterTrades = cash;
        for (AssetPlan p : plans) {
            double tradeCashImpact = -p.sharesDelta * p.holding.getPrice(); // buy reduces cash, sell increases cash
            cashAfterTrades -= tradeCashImpact;
        }
        // If negative cash, reduce buys greedily by one share until >= 0
        if (cashAfterTrades < 0) {
            // Sort buys by largest price first to fix cash quickly
            List<AssetPlan> buys = plans.stream().filter(p -> p.sharesDelta > 0).sorted((a,b) -> Double.compare(b.holding.getPrice(), a.holding.getPrice())).collect(Collectors.toList());
            int idx = 0;
            while (cashAfterTrades < 0 && !buys.isEmpty()) {
                AssetPlan b = buys.get(idx % buys.size());
                if (b.sharesDelta > 0) {
                    b.sharesDelta -= 1;
                    cashAfterTrades += b.holding.getPrice();
                }
                idx++;
                // Remove if no longer a buy
                buys.removeIf(x -> x.sharesDelta <= 0);
            }
        }

        // Filter negligible trades
        List<RebalanceTrade> trades = new ArrayList<>();
        for (AssetPlan p : plans) {
            if (p.sharesDelta == 0) continue;
            double notional = Math.abs(p.sharesDelta * p.holding.getPrice());
            if (notional < params.minTradeNotional) continue;
            RebalanceTrade.Action action = p.sharesDelta > 0 ? RebalanceTrade.Action.BUY : RebalanceTrade.Action.SELL;
            trades.add(new RebalanceTrade(p.holding.getSymbol(), action, Math.abs(p.sharesDelta), p.holding.getPrice()));
        }

        double endingAssetValue = startingAssetValue;
        for (RebalanceTrade t : trades) {
            double delta = t.getShares() * t.getPrice();
            if (t.getAction() == RebalanceTrade.Action.BUY) {
                endingAssetValue += delta;
            } else if (t.getAction() == RebalanceTrade.Action.SELL) {
                endingAssetValue -= delta;
            }
        }
        double endingTotal = endingAssetValue + cashAfterTrades;
        return new RebalanceResult(trades, startingTotal, endingTotal, cashAfterTrades);
    }

    private static long roundTowardZero(double v) {
        return v >= 0 ? (long)Math.floor(v) : (long)Math.ceil(v);
    }

    private static double safePrice(double p) {
        return p == 0.0 ? 1e-9 : p;
    }

    private static class AssetPlan {
        final Holding holding;
        final double targetValue;
        final double deltaValue;
        long sharesDelta;

        AssetPlan(Holding holding, double targetValue, double deltaValue, long sharesDelta) {
            this.holding = holding;
            this.targetValue = targetValue;
            this.deltaValue = deltaValue;
            this.sharesDelta = sharesDelta;
        }
    }
}

