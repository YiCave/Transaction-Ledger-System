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
public class SavingsManager {
    private static final String URL = Constants.getDatabaseURL();
    private static final String USER = Constants.getDatabasaeUSER();
    private static final String PASSWORD = Constants.getDatabasePASSWORD();
    private static final Scanner scanner = new Scanner(System.in);
    
    public static void savings(int userId,String name) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
        String statusQuery = "SELECT status, percentage FROM savings WHERE user_id = ?";
        boolean isActive = false;
        int percentage = 0;

        try (PreparedStatement stmt = connection.prepareStatement(statusQuery)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                isActive = rs.getString("status").equals("active");
                percentage = rs.getInt("percentage");
            }
        }

        System.out.println("== Savings ==");
        if (!isActive) {
            System.out.print("Welcome to savings. Would you like to activate it? (Y/N): ");
            String choice = scanner.nextLine().trim().toUpperCase();
            if (choice.equals("Y")) {
                System.out.print("Please enter the percentage you wish to deduct from the next debit: ");
                percentage = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                // Activate or Reactivate savings
                String activateQuery = "INSERT INTO savings (user_id, status, percentage) VALUES (?, 'active', ?) "
                                     + "ON DUPLICATE KEY UPDATE status = 'active', percentage = ?";
                try (PreparedStatement stmt = connection.prepareStatement(activateQuery)) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, percentage);
                    stmt.setInt(3, percentage);
                    stmt.executeUpdate();
                }
                System.out.println("Savings settings activated successfully!");

                // Show account summary after activation
                AccountManager.showAccountSummary(userId, name); // Pass actual user's name
            } else {
                System.out.println("Savings settings remain inactive.");
            }
        } else {
            System.out.printf("Percentage of savings is currently %d. Do you want to change the savings percentage? (Y/N): ", percentage);
            String choice = scanner.nextLine().trim().toUpperCase();
            if (choice.equals("Y")) {
                System.out.print("Enter new percentage (Simply set to 0 if you wish to deactivate): ");
                percentage = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                // Update savings percentage
                String updateQuery = "UPDATE savings SET percentage = ? WHERE user_id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                    stmt.setInt(1, percentage);
                    stmt.setInt(2, userId);
                    stmt.executeUpdate();
                }
                System.out.println("Savings percentage updated successfully.");

                // Show account summary after update
                AccountManager.showAccountSummary(userId, name); // Pass actual user's name
            } else {

                if (choice.equals("N")) {

                    System.out.printf("Percentage of savings remain unchanged at %d!%n", percentage);

                    // Show account summary after deactivation
                    AccountManager.showAccountSummary(userId, name); // Pass actual user's name
                }
            }
           }
       } catch (SQLException e) {
           System.out.println("Error managing savings: " + e.getMessage());
       }
    }
    
    //add saving history to savings_history table
    public static void savingsHistory(int userId, double savingsAmount) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String get_savings_id = "SELECT savings_id FROM savings WHERE user_id = ? LIMIT 1";
            String insert_history = "INSERT INTO savings_history (user_id, savings_id, amount, date) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
            
            PreparedStatement getSavingsIdStmt = connection.prepareStatement(get_savings_id);
            PreparedStatement insertHistoryStmt = connection.prepareStatement(insert_history);
            
            getSavingsIdStmt.setInt(1, userId);
            ResultSet rs1 = getSavingsIdStmt.executeQuery();
            
            if (rs1.next()) { // Check if there's a result
                int savingsId = rs1.getInt("savings_id"); // Retrieve the savings_id
                insertHistoryStmt.setInt(1, userId);
                insertHistoryStmt.setInt(2, savingsId);
                insertHistoryStmt.setDouble(3, savingsAmount);
                insertHistoryStmt.executeUpdate();
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public static void updateSavingsBalance(int userId, double savingsAmount) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String updateSavingsQuery = "UPDATE savings_balance SET saving = saving + ? WHERE user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(updateSavingsQuery)) {
                stmt.setDouble(1, savingsAmount);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error updating savings balance: " + e.getMessage());
        }
    }
}

