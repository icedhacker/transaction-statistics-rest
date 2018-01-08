package com.mycompany.stats;

import lombok.Data;

@Data
final class Statistics {
    private double sum;
    private double min;
    private double max;
    private long count;
    private double average;

    Statistics() {
        min = Integer.MAX_VALUE;
        max = Integer.MIN_VALUE;
    }

    synchronized void addTransaction(Transaction transaction) {
        count++;
        sum += transaction.getAmount();
        min = transaction.getAmount() < min ? transaction.getAmount() : min;
        max = transaction.getAmount() > max ? transaction.getAmount() : max;
        calculateAverage();
    }

    Statistics addStatistics(Statistics newStatistics) {
        count += newStatistics.getCount();
        sum += newStatistics.getSum();
        min = newStatistics.getMin() < min ? newStatistics.getMin() : min;
        max = newStatistics.getMax() > max ? newStatistics.getMax() : max;
        calculateAverage();
        return this;
    }

    private void calculateAverage() {
        average = count > 0 ? (sum / count) : 0.0;
    }

    boolean isEmpty() {
        return count == 0;
    }
}
