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

    public synchronized void addTransaction(Transaction transaction) {
        count++;
        sum += transaction.getAmount();
        min = transaction.getAmount() < min ? transaction.getAmount() : min;
        max = transaction.getAmount() > max ? transaction.getAmount() : max;
        calculateAverage();
    }

    double calculateAverage() {
        average = count > 0 ? (sum / count) : 0.0;
        return average;
    }

    boolean isEmpty() {
        return count == 0 ? true : false;
    }
}
