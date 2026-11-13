package ui;

import exception.AccountNotFoundException;
import exception.InsufficientFundsException;
import exception.InvalidTransactionException;
import model.BankAccount;
import model.Transaction;
import service.BankService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Scanner;

public class Main {
    private static final BankService bankService = new BankService();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        seedSampleAccounts();
        System.out.println("============================================================");
        System.out.println("   CODENOVA BANK TRANSACTION & ACCOUNT MANAGEMENT SYSTEM    ");
        System.out.println("============================================================");

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Select an option (1-9): ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        createAccountFlow();
                        break;
                    case "2":
                        viewAllAccounts();
                        break;
                    case "3":
                        depositFlow();
                        break;
                    case "4":
                        withdrawFlow();
                        break;
                    case "5":
                        transferFlow();
                        break;
                    case "6":
                        applyInterestFlow();
                        break;
                    case "7":
                        viewHistoryFlow();
                        break;
                    case "8":
                        exportStatementFlow();
                        break;
                    case "9":
                        running = false;
                        System.out.println("Exiting CodeNova Bank System. Goodbye!");
                        break;
                    default:
                        System.out.println("[!] Invalid choice. Please enter a number between 1 and 9.");
                }
            } catch (AccountNotFoundException | InsufficientFundsException | InvalidTransactionException e) {
                System.out.println("[TRANSACTION ERROR] " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("[INPUT ERROR] Please enter a valid numeric amount.");
            } catch (IOException e) {
                System.out.println("[FILE ERROR] Could not export statement: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static void seedSampleAccounts() {
        try {
            bankService.createSavingsAccount("SAV-1001", "Fatima Rida", new BigDecimal("2500.00"), new BigDecimal("0.045"));
            bankService.createCheckingAccount("CHK-2001", "Ali Hassan", new BigDecimal("1200.00"), new BigDecimal("300.00"));
        } catch (InvalidTransactionException ignored) {
        }
    }

    private static void printMenu() {
        System.out.println("\n--- MAIN MENU ---");
        System.out.println("1. Open New Bank Account");
        System.out.println("2. View All Accounts");
        System.out.println("3. Deposit Funds");
        System.out.println("4. Withdraw Funds");
        System.out.println("5. Transfer Funds Between Accounts");
        System.out.println("6. Apply Monthly Interest (Savings)");
        System.out.println("7. View Chronological Transaction History");
        System.out.println("8. Export Account Statement to CSV");
        System.out.println("9. Exit");
    }

    private static void createAccountFlow() throws InvalidTransactionException {
        System.out.print("Account Type (1 = Savings, 2 = Checking): ");
        String type = scanner.nextLine().trim();
        System.out.print("Enter New Account Number (e.g., SAV-1002): ");
        String accNum = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter Account Holder Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Initial Deposit ($): ");
        BigDecimal deposit = new BigDecimal(scanner.nextLine().trim());

        if ("1".equals(type)) {
            bankService.createSavingsAccount(accNum, name, deposit, new BigDecimal("0.045"));
            System.out.println("[SUCCESS] Savings Account " + accNum + " created (Min Balance: $500.00, 4.5% APR).");
        } else if ("2".equals(type)) {
            bankService.createCheckingAccount(accNum, name, deposit, new BigDecimal("300.00"));
            System.out.println("[SUCCESS] Checking Account " + accNum + " created ($300.00 Overdraft Protection).");
        } else {
            System.out.println("[!] Invalid account type selected.");
        }
    }

    private static void viewAllAccounts() {
        System.out.println("\n--- REGISTERED BANK ACCOUNTS ---");
        for (BankAccount acc : bankService.getAllAccounts()) {
            System.out.println(acc);
        }
    }

    private static void depositFlow() throws AccountNotFoundException, InvalidTransactionException {
        System.out.print("Enter Account Number: ");
        String accNum = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter Deposit Amount ($): ");
        BigDecimal amount = new BigDecimal(scanner.nextLine().trim());

        bankService.deposit(accNum, amount);
        System.out.println("[SUCCESS] Deposited $" + amount + " into " + accNum + ". New Balance: $" + bankService.getAccount(accNum).getBalance());
    }

    private static void withdrawFlow() throws AccountNotFoundException, InvalidTransactionException, InsufficientFundsException {
        System.out.print("Enter Account Number: ");
        String accNum = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter Withdrawal Amount ($): ");
        BigDecimal amount = new BigDecimal(scanner.nextLine().trim());

        bankService.withdraw(accNum, amount);
        System.out.println("[SUCCESS] Withdrew $" + amount + " from " + accNum + ". New Balance: $" + bankService.getAccount(accNum).getBalance());
    }

    private static void transferFlow() throws AccountNotFoundException, InvalidTransactionException, InsufficientFundsException {
        System.out.print("Enter Source Account Number: ");
        String fromAcc = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter Destination Account Number: ");
        String toAcc = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter Transfer Amount ($): ");
        BigDecimal amount = new BigDecimal(scanner.nextLine().trim());

        bankService.transferFunds(fromAcc, toAcc, amount);
        System.out.println("[SUCCESS] Transferred $" + amount + " from " + fromAcc + " to " + toAcc + ".");
    }

    private static void applyInterestFlow() throws AccountNotFoundException, InvalidTransactionException {
        System.out.print("Enter Savings Account Number: ");
        String accNum = scanner.nextLine().trim().toUpperCase();
        BigDecimal interest = bankService.applyMonthlyInterestToSavings(accNum);
        System.out.println("[SUCCESS] Monthly interest of $" + interest + " credited to " + accNum + ".");
    }

    private static void viewHistoryFlow() throws AccountNotFoundException {
        System.out.print("Enter Account Number: ");
        String accNum = scanner.nextLine().trim().toUpperCase();
        BankAccount account = bankService.getAccount(accNum);

        System.out.println("\n--- TRANSACTION HISTORY FOR " + accNum + " (" + account.getAccountHolderName() + ") ---");
        for (Transaction tx : account.getTransactionHistory()) {
            System.out.println(tx);
        }
    }
    private static void exportStatementFlow() throws AccountNotFoundException, IOException {
        System.out.print("Enter Account Number to Export Statement: ");
        String accNum = scanner.nextLine().trim().toUpperCase();
        String fileName = bankService.exportStatementToCsv(accNum);
        System.out.println("[SUCCESS] Chronological statement exported to: " + fileName);
    }
}
