/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package latestledgersystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 *
 * @author justb
 */
public class AccountManager {
    private static final String URL = Constants.getDatabaseURL();
    private static final String USER = Constants.getDatabasaeUSER();
    private static final String PASSWORD = Constants.getDatabasePASSWORD();
    private static final Scanner scanner = new Scanner(System.in);
    
    public static void displayWelcomePage(int userId, String name) {
        showAccountSummary(userId, name); // Display welcome with actual name
        LoanManager.checkLoanReminders(userId);

        while (true) {
            System.out.println("\n== Transaction ==");
            System.out.println("1. Debit");
            System.out.println("2. Credit");
            System.out.println("3. History");
            System.out.println("4. Savings");
            System.out.println("5. Credit Loan");
            System.out.println("6. Deposit Interest Predictor");
            System.out.println("7. Data Visualisation");
            System.out.println("8. Logout");
            System.out.print("> ");


            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            VisualizationApp data_visuals = new VisualizationApp(userId);
            switch (choice) {
                case 1 -> debit(userId, name);
                case 2 -> credit(userId, name);
                case 3 -> TransactionManager.history(userId);
                case 4 -> SavingsManager.savings(userId, name); // Pass actual name
                case 5 -> LoanManager.creditLoan(userId);
                case 6 -> InterestPredictor.depositInterestPredictor(userId);
                case 8 -> {
                    System.out.println("Thank you for using our Ledger System services!");
                    return; // Exit the method to log out
                }
                case 7 -> data_visuals.visualizeDataWithCharts();
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public static void showAccountSummary(int userId, String name) { // Accept name as a parameter
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Retrieve the user's balance
            String balanceQuery = "SELECT balance FROM account_balance WHERE user_id = ?";
            double balance = 0.00;
            try (PreparedStatement stmt = connection.prepareStatement(balanceQuery)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    balance = rs.getDouble("balance");
                }
            }

            // Retrieve the user's savings
            String savingsQuery = "SELECT saving FROM savings_balance WHERE user_id = ?";
            double savingsCount = 0.00;
            try (PreparedStatement stmt = connection.prepareStatement(savingsQuery)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    savingsCount = rs.getDouble("saving");
                }
            }

            // Retrieve the user's loans
            String loansQuery = "SELECT outstanding_balance FROM loans WHERE user_id = ? AND status = 'active'"; 
            double loansBalance = 0.00; 
            try (PreparedStatement stmt = connection.prepareStatement(loansQuery)) { 
                stmt.setInt(1, userId); ResultSet rs = stmt.executeQuery(); 
                if (rs.next()) { 
                    loansBalance = rs.getDouble("outstanding_balance"); 
                } 
            }

            // Display the account summary with the user's name
            System.out.printf("== Welcome, %s ==%n", name); // Show user's name instead of user_id
            System.out.printf("Balance: %.2f%n", balance);
            System.out.printf("Savings: %.2f%n", savingsCount);
            System.out.printf("Loans: %.2f%n", loansBalance);
            
        } catch (SQLException e) {
            System.out.println("Error retrieving account summary: " + e.getMessage());
        }
    }

    private static void debit(int userId, String name) {
        if (LoanManager.isLoanPastDue(userId)) { 
            System.out.println("Your loan is past due. Debit and Credit transactions are not allowed until the loan is repaid."); 
            return; 
        }
        
        System.out.println("== Debit Transaction ==");
        double amount = Utils.promptForPositiveAmount();
        String description = Utils.promptForDescription();

        double savingsAmount = 0.00;

        // Check if savings are active
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String checkSavingsQuery = "SELECT status, percentage FROM savings WHERE user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(checkSavingsQuery)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getString("status").equals("active")) {
                    int percentage = rs.getInt("percentage");
                    savingsAmount = (percentage / 100.0) * amount; // Calculate savings deduction
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking savings status: " + e.getMessage());
        }

        // Update the user's balance and savings balance
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String transactionQuery = "INSERT INTO transactions (user_id, transaction_type, amount, description, date) VALUES (?, 'debit', ?, ?, CURRENT_DATE)";
            try (PreparedStatement stmt = connection.prepareStatement(transactionQuery)) {
                stmt.setInt(1, userId);
                stmt.setDouble(2, amount);
                stmt.setString(3, description);
                stmt.executeUpdate();
            }

            // Update the user's account balance (subtracting savings amount)
            updateBalance(userId, amount - savingsAmount);

            // Update savings balance if applicable
            if (savingsAmount > 0) {
                SavingsManager.updateSavingsBalance(userId, savingsAmount);
                SavingsManager.savingsHistory(userId, savingsAmount);
            }

            System.out.println("Debit successfully recorded!");
            showAccountSummary(userId, name); // Pass user's name to display the updated summary
        } catch (SQLException e) {
            System.out.println("Error recording debit transaction: " + e.getMessage());
        }
    }

    private static void credit(int userId, String name) {
        if (LoanManager.isLoanPastDue(userId)) { 
            System.out.println("Your loan is past due. Credit transactions are not allowed until the loan is repaid."); 
            return; 
        }
        
        System.out.println("== Credit Transaction ==");
        double amount = Utils.promptForPositiveAmount();
        String description = Utils.promptForDescription();

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String transactionQuery = "INSERT INTO transactions (user_id, transaction_type, amount, description, date) VALUES (?, 'credit', ?, ?, CURRENT_DATE)";
            try (PreparedStatement stmt = connection.prepareStatement(transactionQuery)) {
                stmt.setInt(1, userId);
                stmt.setDouble(2, amount);
                stmt.setString(3, description);
                stmt.executeUpdate();
            }

            // Update the user's balance
            updateBalance(userId, -amount); // Add amount to the balance
            System.out.println("Credit successfully recorded!");
            showAccountSummary(userId, name); // Pass user's name to display the updated summary
        } catch (SQLException e) {
            System.out.println("Error recording credit transaction: " + e.getMessage());
        }
    }

    //update balance of user
    private static void updateBalance(int userId, double amount) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "UPDATE account_balance SET balance = balance + ? WHERE user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setDouble(1, amount);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error updating balance: " + e.getMessage());
        }
    }
}

