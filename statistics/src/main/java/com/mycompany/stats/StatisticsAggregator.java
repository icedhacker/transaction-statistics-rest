package com.mycompany.stats;

import java.util.Arrays;
import java.util.stream.IntStream;

final class StatisticsAggregator {
    private static long baseTimestampSecs;
    private static long minTimestampInSecs;

    // Storing the statistics in a cyclic buffer of fixed 60 size i.e. 60 seconds data.
    private static Statistics[] statistics = new Statistics[60];

    static void initialize(long currentTimeStampMillis) {
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
        synchronized (statistics) {
            resetStatisticsArray(currentTimestampInMillis / 1000);
            int index = (int) ((timestamp / 1000) - baseTimestampSecs) % 60;
            statistics[index].addTransaction(transaction);
        }
        return true;
    }

    static Statistics getAggregatedStatistics(long currentTimestampInMillis) {
        synchronized (statistics) {
            resetStatisticsArray(currentTimestampInMillis / 1000);
        }
        return Arrays.stream(statistics)
                .reduce(new Statistics(), Statistics::addStatistics);
    }

    private static void resetStatisticsArray(long currentTimestampInSecs) {
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
