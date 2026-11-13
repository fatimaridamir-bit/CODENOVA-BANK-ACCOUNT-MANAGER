package model;

import exception.InsufficientFundsException;
import exception.InvalidTransactionException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BankAccount {
    private final String accountNumber;
    private final String accountHolderName;
    protected BigDecimal balance;
    private final List<Transaction> transactionHistory;

    public BankAccount(String accountNumber, String accountHolderName, BigDecimal initialDeposit) throws InvalidTransactionException {
        if (initialDeposit == null || initialDeposit.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTransactionException("Initial deposit cannot be negative.");
        }
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.balance = initialDeposit.setScale(2, RoundingMode.HALF_UP);
        this.transactionHistory = new ArrayList<>();

        if (this.balance.compareTo(BigDecimal.ZERO) > 0) {
            recordTransaction("DEPOSIT", this.balance, "Initial Account Opening Deposit");
        }
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public List<Transaction> getTransactionHistory() {
        return Collections.unmodifiableList(transactionHistory);
    }

    protected void recordTransaction(String type, BigDecimal amount, String description) {
        transactionHistory.add(new Transaction(type, amount.setScale(2, RoundingMode.HALF_UP), this.balance, description));
    }

    public void deposit(BigDecimal amount) throws InvalidTransactionException {
        validatePositiveAmount(amount);
        BigDecimal scaledAmount = amount.setScale(2, RoundingMode.HALF_UP);
        this.balance = this.balance.add(scaledAmount);
        recordTransaction("DEPOSIT", scaledAmount, "Cash/Online Deposit");
    }

    public abstract void withdraw(BigDecimal amount) throws InvalidTransactionException, InsufficientFundsException;

    public abstract String getAccountType();

    public void debitTransfer(BigDecimal amount, String targetAccountNumber) throws InvalidTransactionException, InsufficientFundsException {
        withdrawForTransfer(amount, "Transfer to Account " + targetAccountNumber);
    }

    public void creditTransfer(BigDecimal amount, String sourceAccountNumber) throws InvalidTransactionException {
        validatePositiveAmount(amount);
        BigDecimal scaledAmount = amount.setScale(2, RoundingMode.HALF_UP);
        this.balance = this.balance.add(scaledAmount);
        recordTransaction("TRANSFER_IN", scaledAmount, "Transfer from Account " + sourceAccountNumber);
    }

    protected abstract void withdrawForTransfer(BigDecimal amount, String description) throws InvalidTransactionException, InsufficientFundsException;

    protected void validatePositiveAmount(BigDecimal amount) throws InvalidTransactionException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transaction amount must be greater than zero.");
        }
    }

    @Override
    public String toString() {
        return String.format("Account [%s] | Type: %-16s | Holder: %-18s | Balance: $%s",
                accountNumber, getAccountType(), accountHolderName, balance.toPlainString());
    }
}

