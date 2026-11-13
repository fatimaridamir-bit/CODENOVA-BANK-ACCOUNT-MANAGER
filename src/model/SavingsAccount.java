package model;

import exception.InsufficientFundsException;
import exception.InvalidTransactionException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SavingsAccount extends BankAccount {
    private static final BigDecimal MINIMUM_BALANCE = new BigDecimal("500.00");
    private final BigDecimal annualInterestRate;

    public SavingsAccount(String accountNumber, String accountHolderName, BigDecimal initialDeposit, BigDecimal annualInterestRate)
            throws InvalidTransactionException {
        super(accountNumber, accountHolderName, initialDeposit);
        if (initialDeposit.compareTo(MINIMUM_BALANCE) < 0) {
            throw new InvalidTransactionException("Savings account requires a minimum initial deposit of $" + MINIMUM_BALANCE);
        }
        this.annualInterestRate = annualInterestRate;
    }

    @Override
    public void withdraw(BigDecimal amount) throws InvalidTransactionException, InsufficientFundsException {
        withdrawInternal(amount, "ATM/Branch Withdrawal", "WITHDRAWAL");
    }

    @Override
    protected void withdrawForTransfer(BigDecimal amount, String description) throws InvalidTransactionException, InsufficientFundsException {
        withdrawInternal(amount, description, "TRANSFER_OUT");
    }

    private void withdrawInternal(BigDecimal amount, String description, String txType) throws InvalidTransactionException, InsufficientFundsException {
        validatePositiveAmount(amount);
        BigDecimal scaledAmount = amount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal projectedBalance = this.balance.subtract(scaledAmount);

        if (projectedBalance.compareTo(MINIMUM_BALANCE) < 0) {
            throw new InsufficientFundsException(
                    String.format("Withdrawal denied. Savings accounts must maintain a minimum balance of $%s. Available to withdraw: $%s",
                            MINIMUM_BALANCE, this.balance.subtract(MINIMUM_BALANCE).max(BigDecimal.ZERO))
            );
        }
        this.balance = projectedBalance;
        recordTransaction(txType, scaledAmount, description);
    }

    public BigDecimal applyMonthlyInterest() {
        BigDecimal monthlyRate = annualInterestRate.divide(new BigDecimal("12"), 6, RoundingMode.HALF_UP);
        BigDecimal interest = this.balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
        if (interest.compareTo(BigDecimal.ZERO) > 0) {
            this.balance = this.balance.add(interest);
            recordTransaction("INTEREST", interest, "Monthly Interest Credited (" + annualInterestRate.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString() + "% p.a.)");
        }
        return interest;
    }

    @Override
    public String getAccountType() {
        return "Savings Account";
    }
}

