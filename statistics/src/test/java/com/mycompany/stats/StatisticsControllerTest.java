package com.mycompany.stats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        StatisticsAggregator.initialize(System.currentTimeMillis());
    }

    @Test
    public void shouldReturn204OnPostingTransactionMoreThan60SecondsOld() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                .content(convertToJson(new Transaction(System.currentTimeMillis() - 80000, 100.00)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void shouldReturn200OnPostingTransactionLessThan60Seconds() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                .content(convertToJson(new Transaction(System.currentTimeMillis() - 20000, 100.00)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnProperStatisticsAfterPostingSingleTransactions() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                .content(convertToJson(new Transaction(System.currentTimeMillis() - 20000, 100.00)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/statistics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.sum", is(100.00)))
                .andExpect(jsonPath("$.count", is(1)))
                .andExpect(jsonPath("$.min", is(100.00)))
                .andExpect(jsonPath("$.max", is(100.00)))
                .andExpect(jsonPath("$.average", is(100.00)));
    }

    @Test
    public void shouldReturnProperStatisticsAfterPostingMultipleTransactions() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                .content(convertToJson(new Transaction(System.currentTimeMillis() - 55000, 100.00)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/transactions")
                .content(convertToJson(new Transaction(System.currentTimeMillis() - 30000, 200.00)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/transactions")
                .content(convertToJson(new Transaction(System.currentTimeMillis() - 20000, 300.00)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/transactions")
                .content(convertToJson(new Transaction(System.currentTimeMillis() - 20000, 400.00)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Adding a sleep for 8 seconds to test whether the first transaction, which was 55 seconds ago data, is deleted.
        TimeUnit.SECONDS.sleep(8);

        mockMvc.perform(get("/api/v1/statistics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.sum", is(900.00)))
                .andExpect(jsonPath("$.count", is(3)))
                .andExpect(jsonPath("$.min", is(200.00)))
                .andExpect(jsonPath("$.max", is(400.00)))
                .andExpect(jsonPath("$.average", is(300.00)));
    }

    @Test
    public void shouldReturnProperStatisticsAfterConcurrentlyPostingMultipleTransactions() throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(() ->
                mockMvc.perform(post("/api/v1/transactions")
                        .content(convertToJson(new Transaction(System.currentTimeMillis() - 55000, 100.00)))
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk()));
        executorService.submit(() ->
                mockMvc.perform(post("/api/v1/transactions")
                        .content(convertToJson(new Transaction(System.currentTimeMillis() - 30000, 200.00)))
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk()));
        executorService.submit(() ->
                mockMvc.perform(post("/api/v1/transactions")
                        .content(convertToJson(new Transaction(System.currentTimeMillis() - 20000, 300.00)))
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk()));
        executorService.submit(() ->
                mockMvc.perform(post("/api/v1/transactions")
                        .content(convertToJson(new Transaction(System.currentTimeMillis() - 20000, 400.00)))
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk()));
        executorService.awaitTermination(1000, TimeUnit.MILLISECONDS);

        // Adding a sleep for 8 seconds to test whether the first transaction, which was 55 seconds ago data, is deleted.
        TimeUnit.SECONDS.sleep(8);

        mockMvc.perform(get("/api/v1/statistics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.sum", is(900.00)))
                .andExpect(jsonPath("$.count", is(3)))
                .andExpect(jsonPath("$.min", is(200.00)))
                .andExpect(jsonPath("$.max", is(400.00)))
                .andExpect(jsonPath("$.average", is(300.00)));
    }

    private String convertToJson(Transaction transaction) {
        try {
            return objectMapper.writeValueAsString(transaction);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

}
