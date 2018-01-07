package com.mycompany.stats;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class StatisticsController {

    public StatisticsController() {
        StatisticsAggregator.initialize(System.currentTimeMillis());
    }

    @RequestMapping(value = "/transactions", method = RequestMethod.POST)
    public ResponseEntity<Void> addTransaction(@RequestBody Transaction transaction) {
        if (StatisticsAggregator.addTransaction(transaction, System.currentTimeMillis())) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/statistics", method = RequestMethod.GET)
    public Statistics getStatistics() {
        return StatisticsAggregator.getAggregatedStatistics(System.currentTimeMillis());
    }
}
