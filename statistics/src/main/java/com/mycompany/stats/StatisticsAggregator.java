package com.mycompany.stats;

import java.util.stream.IntStream;

final class StatisticsAggregator {
    private static long baseTimestampSecs;
    private static long minTimestampInSecs;

    // Storing the statistics in a cyclic buffer of fixed 60 size i.e. 60 seconds data.
    private static Statistics[] statistics = new Statistics[60];

    public static void initialize(long currentTimeStampMillis) {
        baseTimestampSecs = (currentTimeStampMillis / 1000) - 59;
        minTimestampInSecs = baseTimestampSecs;
        IntStream.range(0, statistics.length).forEach(
                i -> statistics[i] = new Statistics()
        );
    }

    private StatisticsAggregator() {
    }

    static boolean addTransaction(Transaction transaction, long currentTimestampInMillis) {
        long timestamp = transaction.getTimestamp();
        if ((currentTimestampInMillis - timestamp) > 60000) {
            return false;
        }
        resetStatisticsArray(currentTimestampInMillis / 1000);
        int index = (int) ((timestamp / 1000) - baseTimestampSecs) % 60;
        statistics[index].addTransaction(transaction);
        return true;
    }

    static Statistics getAggregatedStatistics(long currentTimestampInMillis) {
        resetStatisticsArray(currentTimestampInMillis / 1000);
        Statistics aggregatedStats = new Statistics();
        for (Statistics statistic : statistics) {
            if (statistic.getCount() > 0) {
                aggregatedStats.setCount(aggregatedStats.getCount() + statistic.getCount());
                aggregatedStats.setSum(aggregatedStats.getSum() + statistic.getSum());
                aggregatedStats.setMax(
                        statistic.getMax() > aggregatedStats.getMax() ? statistic.getMax() : aggregatedStats.getMax());
                aggregatedStats.setMin(
                        statistic.getMin() < aggregatedStats.getMin() ? statistic.getMin() : aggregatedStats.getMin());
            }
        }
        aggregatedStats.calculateAverage();
        return aggregatedStats;
    }

    private static synchronized void resetStatisticsArray(long currentTimestampInSecs) {
        long currentMinTimestamp = currentTimestampInSecs - 59;
        long difference = currentMinTimestamp - minTimestampInSecs;
        int start = 0;
        int end = 60;
        if (difference == 0) {
            return;
        }
        if (difference < 60) {
            start = (int) (minTimestampInSecs - baseTimestampSecs) % 60;
            end = (int) (currentMinTimestamp - baseTimestampSecs) % 60;
        }
        if (start < end) {
            for (int i = start; i < end; i++) {
                statistics[i] = new Statistics();
            }
        } else {
            for (int i = start; i < 60; i++) {
                statistics[i] = new Statistics();
            }
            for (int i = 0; i < end; i++) {
                statistics[i] = new Statistics();
            }
            if (start == end) {
                statistics[end] = new Statistics();
            }
        }
        minTimestampInSecs = currentMinTimestamp;
    }

    static Statistics[] getStatistics() {
        return statistics;
    }
}
