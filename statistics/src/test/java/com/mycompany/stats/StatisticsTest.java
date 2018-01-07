package com.mycompany.stats;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class StatisticsTest {

    private static final long TEST_TIMESTAMP = System.currentTimeMillis();
    private static final double TEST_AMOUNT = 100.00;
    private static final double TEST_SUM = 300.00;
    private static final double TEST_CONCURRENT_SUM = 500.00;

    private Statistics statistics;

    @Before
    public void setup() {
        statistics = new Statistics();
    }

    @Test
    public void defaultStatisticsIsEmptyTrue() {
        assertThat(statistics.isEmpty()).isTrue();
    }

    @Test
    public void checkIsEmptyFalseAfterAddingTransaction() {
        Transaction transaction = new Transaction(TEST_TIMESTAMP, TEST_AMOUNT);
        statistics.addTransaction(transaction);
        assertThat(statistics.isEmpty()).isFalse();
    }

    @Test
    public void checkAllStatisticsAfterAddingOneTransaction() {
        Transaction transaction = new Transaction(TEST_TIMESTAMP, TEST_AMOUNT);
        statistics.addTransaction(transaction);
        assertThat(statistics.isEmpty()).isFalse();
        assertThat(statistics.getCount()).isEqualTo(1);
        assertThat(statistics.getSum()).isEqualTo(TEST_AMOUNT);
        assertThat(statistics.getMax()).isEqualTo(TEST_AMOUNT);
        assertThat(statistics.getMin()).isEqualTo(TEST_AMOUNT);
        assertThat(statistics.getAverage()).isEqualTo(TEST_AMOUNT);
    }

    @Test
    public void checkAllStatisticsAfterAddingMultipleTransaction() {
        Transaction transaction = new Transaction(TEST_TIMESTAMP, TEST_AMOUNT);
        statistics.addTransaction(transaction);
        statistics.addTransaction(transaction);
        statistics.addTransaction(transaction);
        assertThat(statistics.isEmpty()).isFalse();
        assertThat(statistics.getCount()).isEqualTo(3);
        assertThat(statistics.getSum()).isEqualTo(TEST_SUM);
        assertThat(statistics.getMax()).isEqualTo(TEST_AMOUNT);
        assertThat(statistics.getMin()).isEqualTo(TEST_AMOUNT);
        assertThat(statistics.getAverage()).isEqualTo(TEST_AMOUNT);
    }

    @Test
    public void checkMultiThreadSyncAddingMultipleTransactions() throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        Transaction transaction = new Transaction(TEST_TIMESTAMP, TEST_AMOUNT);
        Runnable task = () -> statistics.addTransaction(transaction);
        IntStream.range(0, 5).forEach(count -> executorService.submit(task));
        executorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
        assertThat(statistics.isEmpty()).isFalse();
        assertThat(statistics.getCount()).isEqualTo(5);
        assertThat(statistics.getSum()).isEqualTo(TEST_CONCURRENT_SUM);
        assertThat(statistics.getMax()).isEqualTo(TEST_AMOUNT);
        assertThat(statistics.getMin()).isEqualTo(TEST_AMOUNT);
        assertThat(statistics.getAverage()).isEqualTo(TEST_AMOUNT);
    }
}
