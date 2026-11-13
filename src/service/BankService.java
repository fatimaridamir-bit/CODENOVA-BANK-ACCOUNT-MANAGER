package service;

import exception.AccountNotFoundException;
import exception.InsufficientFundsException;
import exception.InvalidTransactionException;
import model.BankAccount;
import model.CheckingAccount;
import model.SavingsAccount;
import model.Transaction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class BankService {
    private final Map<String, BankAccount> accounts = new LinkedHashMap<>();

    public void createSavingsAccount(String accNumber, String holderName, BigDecimal initialDeposit, BigDecimal interestRate)
            throws InvalidTransactionException {
        if (accounts.containsKey(accNumber)) {
            throw new InvalidTransactionException("Account number " + accNumber + " already exists.");
        }
        accounts.put(accNumber, new SavingsAccount(accNumber, holderName, initialDeposit, interestRate));
    }

    public void createCheckingAccount(String accNumber, String holderName, BigDecimal initialDeposit, BigDecimal overdraftLimit)
            throws InvalidTransactionException {
        if (accounts.containsKey(accNumber)) {
            throw new InvalidTransactionException("Account number " + accNumber + " already exists.");
        }
        accounts.put(accNumber, new CheckingAccount(accNumber, holderName, initialDeposit, overdraftLimit));
    }

    public BankAccount getAccount(String accNumber) throws AccountNotFoundException {
        BankAccount account = accounts.get(accNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account not found with number: " + accNumber);
        }
        return account;
    }

    public Collection<BankAccount> getAllAccounts() {
        return accounts.values();
    }

    public void deposit(String accNumber, BigDecimal amount) throws AccountNotFoundException, InvalidTransactionException {
        BankAccount account = getAccount(accNumber);
        account.deposit(amount);
    }

    public void withdraw(String accNumber, BigDecimal amount)
            throws AccountNotFoundException, InvalidTransactionException, InsufficientFundsException {
        BankAccount account = getAccount(accNumber);
        account.withdraw(amount);
    }

    public void transferFunds(String fromAccNumber, String toAccNumber, BigDecimal amount)
            throws AccountNotFoundException, InvalidTransactionException, InsufficientFundsException {
        if (fromAccNumber.equals(toAccNumber)) {
            throw new InvalidTransactionException("Source and destination accounts cannot be the same.");
        }
        BankAccount source = getAccount(fromAccNumber);
        BankAccount destination = getAccount(toAccNumber);

        source.debitTransfer(amount, toAccNumber);
        destination.creditTransfer(amount, fromAccNumber);
    }

    public BigDecimal applyMonthlyInterestToSavings(String accNumber)
            throws AccountNotFoundException, InvalidTransactionException {
        BankAccount account = getAccount(accNumber);
        if (!(account instanceof SavingsAccount)) {
            throw new InvalidTransactionException("Interest calculation applies only to Savings Accounts.");
        }
        return ((SavingsAccount) account).applyMonthlyInterest();
    }

    public String exportStatementToCsv(String accNumber) throws AccountNotFoundException, IOException {
        BankAccount account = getAccount(accNumber);
        String fileName = "statement_" + accNumber + ".csv";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("Account Number," + account.getAccountNumber());
            writer.newLine();
            writer.write("Account Holder," + account.getAccountHolderName());
            writer.newLine();
            writer.write("Account Type," + account.getAccountType());
            writer.newLine();
            writer.write("Current Balance,$" + account.getBalance().toPlainString());
            writer.newLine();
            writer.newLine();

            writer.write("Transaction ID,Timestamp,Type,Amount ($),Balance After ($),Description");
            writer.newLine();
            for (Transaction tx : account.getTransactionHistory()) {
                writer.write(tx.toCsvRow());
                writer.newLine();
            }
        }
        return fileName;
    }
}
