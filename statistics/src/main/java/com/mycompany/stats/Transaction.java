package com.mycompany.stats;

import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class Transaction {
    private long timestamp;
    private double amount;
}
