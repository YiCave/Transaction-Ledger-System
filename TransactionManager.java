/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package latestledgersystem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Scanner;

/**
 *
 * @author justb
 */
public class TransactionManager {
    private static final Scanner scanner = new Scanner(System.in);
    private static final String URL = Constants.getDatabaseURL();
    private static final String USER = Constants.getDatabasaeUSER();
    private static final String PASSWORD = Constants.getDatabasePASSWORD();
    
    public static void history(int userId) {
        System.out.print("Do You Wish to Sort or Filter Your Transaction History? (Y/N): ");
        String userChoice = scanner.next();
        scanner.nextLine(); // Consume newline

        if (userChoice.equalsIgnoreCase("Y")) {
            System.out.println("""
                    You Wish To?
                    1. Sort
                    2. Filter
                    """);
            System.out.print(">");
            int sortorfilter = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (sortorfilter) {
                case 1 -> sortTransactions(userId);
                case 2 -> filterTransactions(userId);
                default -> System.out.println("Invalid option. You can only choose 1 or 2");
            }
        } else if (userChoice.equalsIgnoreCase("N")) {
            showRecentTransactions(userId);
        } else {
            System.out.println("Invalid input, please only enter Y or N.");
        }
    }

    // Helper to display recent transactions
    private static void showRecentTransactions(int userId) {
        String query = "SELECT transaction_id, transaction_type, amount, description, date FROM transactions WHERE user_id = ?";
        Calendar calendar = Calendar.getInstance();

        try (
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement stmt = connection.prepareStatement(query)
        ) {
            stmt.setInt(1, userId);


            ResultSet rs = stmt.executeQuery();
            displayTransactions(rs);
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Helper for sorting transactions
    private static void sortTransactions(int userId) {
        System.out.println("""
                Sort By?
                1. Date
                2. Amount
                """);
        System.out.print(">");
        int sortdateoramount = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        String sortColumn = switch (sortdateoramount) {
            case 1 -> "date";
            case 2 -> "amount";
            default -> null;
        };

        if (sortColumn == null) {
            System.out.println("Invalid option. Please choose 1 or 2.");
            return;
        }

        System.out.print("Order (ASC/DESC): ");
        String sortby = scanner.nextLine(); // This will now work correctly

        String query = "SELECT transaction_id, transaction_type, amount, description, date FROM transactions WHERE user_id = ? ORDER BY " + sortColumn + " " + sortby;

        try (
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement stmt = connection.prepareStatement(query)
        ) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            displayTransactions(rs);
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Helper for filtering transactions
    private static void filterTransactions(int userId) {
        System.out.println("""
                Filter by?
                1. Date Range
                2. Transaction Type
                3. Amount Range
                """);
        System.out.print(">");
        int filterby = scanner.nextInt();
        scanner.nextLine();

        String query = "SELECT transaction_id, transaction_type, amount, description, date FROM transactions WHERE user_id = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);

            switch (filterby) {

                case 1 -> {
                    System.out.print("Enter start date (YYYY-MM-DD): ");
                    String startDate = scanner.nextLine();
                    System.out.print("Enter end date (YYYY-MM-DD): ");
                    String endDate = scanner.nextLine();

                    query += " AND date BETWEEN ? AND ?";
                    stmt = connection.prepareStatement(query);

                    stmt.setInt(1, userId);
                    stmt.setString(2, startDate);
                    stmt.setString(3, endDate);
                }

                case 2 -> {
                    System.out.print("Enter transaction type (debit/credit): ");
                    String transactionType = scanner.nextLine();

                    query += " AND transaction_type = ?";
                    stmt = connection.prepareStatement(query);
                    stmt.setInt(1, userId);
                    stmt.setString(2, transactionType);
                }

                case 3 -> {
                    System.out.print("Enter minimum amount: ");
                    double minAmount = scanner.nextDouble();
                    System.out.print("Enter maximum amount: ");
                    double maxAmount = scanner.nextDouble();
                    scanner.nextLine(); // Consume newline
                    query += " AND amount BETWEEN ? AND ?";
                    stmt = connection.prepareStatement(query);
                    stmt.setInt(1, userId);
                    stmt.setDouble(2, minAmount);
                    stmt.setDouble(3, maxAmount);
                }
                default -> {
                    System.out.println("Invalid option.");
                    return;
                }
            }

            ResultSet rs = stmt.executeQuery();
            displayTransactions(rs);
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Helper to display transactions
    private static void displayTransactions(ResultSet rs) {
        try {
            // Print column headers
            System.out.printf("%-10s %-15s %-30s %-10s %-10s %-10s%n", 
                "Index","Date ","Description","Amount","Type", "Cumm.Balance"  );
            System.out.println("=".repeat(90));

            // Process the result set
            double total = 0;
            StringBuilder csvBuilder = new StringBuilder();
            csvBuilder.append("Index,Date,Description,Amount,Type,Cumm.Balance\n");

            int j = 1;
            while (rs.next()) {
                String transType = rs.getString("transaction_type");
                double amount = rs.getDouble("amount");
                String description = rs.getString("description");
                String date = rs.getString("date");

                // Print each row of data
                System.out.printf("%-10d %-15s", j, date);
                j++;

                // Wrap the description text
                // Calculate balance
                int descriptionWidth = 30;
                String[] lines = Utils.wrapText(description, descriptionWidth);
                for (int i = 0; i < lines.length; i++) {
                    if (i == 0) {
                        total = "debit".equals(transType) ? total + amount : total - amount; 
                        // Print the first line of the description and date
                        System.out.printf(" %-30s %-10.2f %-10s %-10s%n", lines[i], amount, transType, total);
                    } else {
                        // Print subsequent lines of the description in the same column
                        System.out.printf(" %-25s %-30s%n", "", lines[i]);
                    }
                }

                // Add to CSV content
                csvBuilder.append(String.format("%d,%s,%s,%.2f,%s,%.2f\n",
                    j, date, description, amount, transType, total));
            }

            // Prompt for CSV export
            System.out.print("Do you want to export the history to a CSV file? (yes/no): ");
            String exportChoice = scanner.nextLine();

            if ("yes".equalsIgnoreCase(exportChoice)) {
                Utils.exportToCSV(csvBuilder.toString());
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}


