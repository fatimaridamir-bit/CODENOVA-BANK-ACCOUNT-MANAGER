package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Transaction {
    private final String transactionId;
    private final String type;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final LocalDateTime timestamp;
    private final String description;

    public Transaction(String type, BigDecimal amount, BigDecimal balanceAfter, String description) {
        this.transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = LocalDateTime.now();
        this.description = description;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String toCsvRow() {
        return String.join(",",
                transactionId,
                getFormattedTimestamp(),
                type,
                amount.toPlainString(),
                balanceAfter.toPlainString(),
                "\"" + description + "\""
        );
    }

    @Override
    public String toString() {
        return String.format("[%s] %-12s | %-12s | Amount: $%-10s | Balance: $%-10s | %s",
                getFormattedTimestamp(), transactionId, type, amount.toPlainString(), balanceAfter.toPlainString(), description);
    }
}

