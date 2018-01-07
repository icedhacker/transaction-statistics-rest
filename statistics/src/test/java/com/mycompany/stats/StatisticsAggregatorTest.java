package com.mycompany.stats;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class StatisticsAggregatorTest {

    private static final double TEST_AMOUNT = 100.00;
    private static final double TEST_AMOUNT2 = 200.00;
    private static final double TEST_AMOUNT3 = 300.00;
    private static final double TEST_AMOUNT4 = 400.00;

    private static final double TEST_SUM = 1000.00;
    private static final double TEST_MIN = 100.00;
    private static final double TEST_MAX = 400.00;
    private static final double TEST_AVG = 250.00;

    private static final long MOCK_TIME_STAMP_IN_MILLIS = 1515324450000L;


    @Before
    public void setup() {
        StatisticsAggregator.initialize(MOCK_TIME_STAMP_IN_MILLIS);
    }

    @Test
    public void shouldNotAddTransactionIfExpiredTimestamp() {
        Transaction transaction = new Transaction(MOCK_TIME_STAMP_IN_MILLIS - 100000, TEST_AMOUNT);
        boolean result = StatisticsAggregator.addTransaction(transaction, MOCK_TIME_STAMP_IN_MILLIS + 1000);
        assertThat(result).isFalse();
    }

    @Test
    public void shouldReturnBlankStatisticsIfNoTransaction() {
        Statistics statistics = StatisticsAggregator.getAggregatedStatistics(MOCK_TIME_STAMP_IN_MILLIS + 10000);
        assertThat(statistics.isEmpty()).isTrue();
    }

    @Test
    public void checkDefaultStatisticsHas60Elements() {
        Statistics[] statistics = StatisticsAggregator.getStatistics();
        assertThat(statistics.length).isEqualTo(60);
    }

    @Test
    public void checkStatisticsOnAddingTransaction() {
        Transaction transaction = new Transaction(MOCK_TIME_STAMP_IN_MILLIS + 10000, TEST_AMOUNT);
        StatisticsAggregator.addTransaction(transaction, MOCK_TIME_STAMP_IN_MILLIS + 15000);
        Statistics statistics = StatisticsAggregator.getAggregatedStatistics(MOCK_TIME_STAMP_IN_MILLIS + 20000);
        assertThat(statistics).isNotNull();
        assertThat(statistics.isEmpty()).isFalse();
        assertThat(statistics.getCount()).isEqualTo(1);
        assertThat(statistics.getSum()).isEqualTo(TEST_AMOUNT);
        assertThat(statistics.getMin()).isEqualTo(TEST_AMOUNT);
        assertThat(statistics.getMax()).isEqualTo(TEST_AMOUNT);
        assertThat(statistics.getAverage()).isEqualTo(TEST_AMOUNT);
    }

    @Test
    public void checkStatisticsOnAddingMultipleTransactionsWithDifferentTimestamps() {
        List<Transaction> transactionList = new ArrayList<>();
        transactionList.add(new Transaction(MOCK_TIME_STAMP_IN_MILLIS + 1000, TEST_AMOUNT));
        transactionList.add(new Transaction(MOCK_TIME_STAMP_IN_MILLIS + 2000, TEST_AMOUNT2));
        transactionList.add(new Transaction(MOCK_TIME_STAMP_IN_MILLIS + 3000, TEST_AMOUNT3));
        transactionList.add(new Transaction(MOCK_TIME_STAMP_IN_MILLIS + 4000, TEST_AMOUNT4));
        transactionList.forEach(
                transaction -> StatisticsAggregator.addTransaction(
                        transaction, MOCK_TIME_STAMP_IN_MILLIS + 10000)
        );
        Statistics statistics = StatisticsAggregator.getAggregatedStatistics(MOCK_TIME_STAMP_IN_MILLIS + 20000);
        assertThat(statistics).isNotNull();
        assertThat(statistics.isEmpty()).isFalse();
        assertThat(statistics.getCount()).isEqualTo(4);
        assertThat(statistics.getSum()).isEqualTo(TEST_SUM);
        assertThat(statistics.getMin()).isEqualTo(TEST_MIN);
        assertThat(statistics.getMax()).isEqualTo(TEST_MAX);
        assertThat(statistics.getAverage()).isEqualTo(TEST_AVG);
    }

    @Test
    public void checkStatisticsOnAddingMultipleTransactionsWithSameTimestamps() {
        List<Transaction> transactionList = new ArrayList<>();
        transactionList.add(new Transaction(MOCK_TIME_STAMP_IN_MILLIS + 2000, TEST_AMOUNT));
        transactionList.add(new Transaction(MOCK_TIME_STAMP_IN_MILLIS + 2000, TEST_AMOUNT2));
        transactionList.add(new Transaction(MOCK_TIME_STAMP_IN_MILLIS + 2000, TEST_AMOUNT3));
        transactionList.add(new Transaction(MOCK_TIME_STAMP_IN_MILLIS + 2000, TEST_AMOUNT4));
        transactionList.forEach(
                transaction -> StatisticsAggregator.addTransaction(
                        transaction, MOCK_TIME_STAMP_IN_MILLIS + 10000)
        );
        Statistics statistics = StatisticsAggregator.getAggregatedStatistics(MOCK_TIME_STAMP_IN_MILLIS + 20000);
        assertThat(statistics).isNotNull();
        assertThat(statistics.isEmpty()).isFalse();
        assertThat(statistics.getCount()).isEqualTo(4);
        assertThat(statistics.getSum()).isEqualTo(TEST_SUM);
        assertThat(statistics.getMin()).isEqualTo(TEST_MIN);
        assertThat(statistics.getMax()).isEqualTo(TEST_MAX);
        assertThat(statistics.getAverage()).isEqualTo(TEST_AVG);
    }

    @Test
    public void checkStatisticsOnConcurrentAddingMultipleTransactions() throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Transaction> transactionList = new ArrayList<>();
        transactionList.add(new Transaction(MOCK_TIME_STAMP_IN_MILLIS + 1000, TEST_AMOUNT));
        transactionList.add(new Transaction(MOCK_TIME_STAMP_IN_MILLIS + 2000, TEST_AMOUNT2));
        transactionList.add(new Transaction(MOCK_TIME_STAMP_IN_MILLIS + 2000, TEST_AMOUNT3));
        transactionList.add(new Transaction(MOCK_TIME_STAMP_IN_MILLIS + 4000, TEST_AMOUNT4));
        transactionList.forEach(
                transaction -> executorService.submit(() -> StatisticsAggregator.addTransaction(
                        transaction, MOCK_TIME_STAMP_IN_MILLIS + 10000))
        );
        executorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
        Statistics statistics = StatisticsAggregator.getAggregatedStatistics(MOCK_TIME_STAMP_IN_MILLIS + 20000);
        assertThat(statistics).isNotNull();
        assertThat(statistics.isEmpty()).isFalse();
        assertThat(statistics.getCount()).isEqualTo(4);
        assertThat(statistics.getSum()).isEqualTo(TEST_SUM);
        assertThat(statistics.getMin()).isEqualTo(TEST_MIN);
        assertThat(statistics.getMax()).isEqualTo(TEST_MAX);
        assertThat(statistics.getAverage()).isEqualTo(TEST_AVG);
    }
}
