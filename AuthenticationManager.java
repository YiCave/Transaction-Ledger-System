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
import java.sql.Statement;
import java.util.Scanner;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author justb
 */
public class AuthenticationManager {
    
    private static final String URL = Constants.getDatabaseURL();
    private static final String USER = Constants.getDatabasaeUSER();
    private static final String PASSWORD = Constants.getDatabasePASSWORD();
    private static final Scanner scanner = new Scanner(System.in);
    
    public static void register() {
        
        System.out.println("== Registration ==");

        String name = Utils.promptForValidName();
        String email = Utils.promptForValidEmail();
        String rawPassword = Utils.promptForValidPassword();

        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
 
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "INSERT INTO user (name, email, password) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, name);
                stmt.setString(2, email);
                stmt.setString(3, hashedPassword);
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    initializeAccountData(userId);
                    System.out.println("Registration Successful!");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    public static boolean login() {
        System.out.println("== Login ==");
        System.out.print("Email: ");
        String email = scanner.nextLine();
        //String email = testgmail;
        System.out.print("Password: ");
        String enteredPassword = scanner.nextLine();
        //String enteredPassword = testpass;

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT user_id, name, password FROM user WHERE email = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String storedHashedPassword = rs.getString("password");

                    // Compare the hashed password with the entered password
                    if (BCrypt.checkpw(enteredPassword, storedHashedPassword)) {
                        System.out.println("Login Successful!\n");
                        int user_id = rs.getInt(1);
                        String name = rs.getString("name");
                        AccountManager.displayWelcomePage(user_id, name);
                    } else {
                        System.out.println("Invalid email or password.");
                    }
                } else {
                    System.out.println("Invalid email or password.");
                }
        }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }

        return false;
    }
    
    public static void initializeAccountData(int userId) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Initialize account balance
            String balanceQuery = "INSERT INTO account_balance (user_id, balance) VALUES (?, 0.00)";
            try (PreparedStatement stmt = connection.prepareStatement(balanceQuery)) {
                
                stmt.setInt(1, userId);
                stmt.executeUpdate();
            }

            // Initialize savings
            String savingsQuery = "INSERT INTO savings_balance (user_id, saving) VALUES (?, 0.00)";
            try (PreparedStatement stmt = connection.prepareStatement(savingsQuery)) {
                stmt.setInt(1, userId);
                stmt.executeUpdate();
            }

            // Initialize loan
            String loanQuery = "INSERT INTO loans (user_id, principal_amount, interest_rate, repayment_period, outstanding_balance, status) VALUES (?, 0.00, 0.00, 0, 0.00, 'inactive')";
            try (PreparedStatement stmt = connection.prepareStatement(loanQuery)) {
                stmt.setInt(1, userId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error initializing account data: " + e.getMessage());
        }
    }
    
    public static void updateUnencryptedPasswords() {
        String selectQuery = "SELECT user_id, password FROM user";
        String updateQuery = "UPDATE user SET password = ? WHERE user_id = ?";

        try (
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
            PreparedStatement updateStmt = connection.prepareStatement(updateQuery)
        ) {
            // Fetch all users
            ResultSet rs = selectStmt.executeQuery();

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String password = rs.getString("password");

                // Check if the password is hashed
                if (!Utils.isPasswordHashed(password)) {
                    // Hash the password
                    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                    // Update the database
                    updateStmt.setString(1, hashedPassword);
                    updateStmt.setInt(2, userId);
                    updateStmt.executeUpdate();

                    System.out.println("Updated password for user_id: " + userId);
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }   
    }
}
