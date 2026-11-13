package model;

import exception.InsufficientFundsException;
import exception.InvalidTransactionException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CheckingAccount extends BankAccount {
    private final BigDecimal overdraftLimit;

    public CheckingAccount(String accountNumber, String accountHolderName, BigDecimal initialDeposit, BigDecimal overdraftLimit)
            throws InvalidTransactionException {
        super(accountNumber, accountHolderName, initialDeposit);
        if (overdraftLimit == null || overdraftLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTransactionException("Overdraft limit cannot be negative.");
        }
        this.overdraftLimit = overdraftLimit.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public void withdraw(BigDecimal amount) throws InvalidTransactionException, InsufficientFundsException {
        withdrawInternal(amount, "Checking Withdrawal", "WITHDRAWAL");
    }

    @Override
    protected void withdrawForTransfer(BigDecimal amount, String description) throws InvalidTransactionException, InsufficientFundsException {
        withdrawInternal(amount, description, "TRANSFER_OUT");
    }

    private void withdrawInternal(BigDecimal amount, String description, String txType) throws InvalidTransactionException, InsufficientFundsException {
        validatePositiveAmount(amount);
        BigDecimal scaledAmount = amount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal maxAvailable = this.balance.add(overdraftLimit);

        if (scaledAmount.compareTo(maxAvailable) > 0) {
            throw new InsufficientFundsException(
                    String.format("Insufficient funds. Requested: $%s, Max available (including $%s overdraft): $%s",
                            scaledAmount, overdraftLimit, maxAvailable)
            );
        }
        this.balance = this.balance.subtract(scaledAmount);
        recordTransaction(txType, scaledAmount, description);
    }

    @Override
    public String getAccountType() {
        return "Checking Account";
    }
}

