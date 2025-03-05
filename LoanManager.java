/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package latestledgersystem;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
/**
 *
 * @author justb
 */
public class LoanManager {
    private static final String URL = Constants.getDatabaseURL();
    private static final String USER = Constants.getDatabasaeUSER();
    private static final String PASSWORD = Constants.getDatabasePASSWORD();
    private static final Scanner scanner = new Scanner(System.in);
    
    public static boolean isLoanPastDue(int userId) { 
        String sqlQuery = "SELECT outstanding_balance, repayment_date FROM loans WHERE user_id = ? AND status = 'active'"; 
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) { 
            try (PreparedStatement pstmtQuery = conn.prepareStatement(sqlQuery)) { 
                pstmtQuery.setInt(1, userId); 
                ResultSet rs = pstmtQuery.executeQuery(); 

                if (rs.next()) { 
                    double outstandingBalance = rs.getDouble("outstanding_balance"); 
                    Date repaymentDate = rs.getDate("repayment_date"); 
                    LocalDate currentDate = LocalDate.now(); 
                    if (currentDate.isAfter(repaymentDate.toLocalDate()) && outstandingBalance > 0) { 
                        return true; 
                    } 
                } 
            } 
        } 
        catch (SQLException e) { 
            System.out.println("Error querying data from loans table: " + e.getMessage()); 
        } 
        return false; 
    }
    
    public static void checkLoanReminders(int userId){
        String sqlQuery = "SELECT outstanding_balance, repayment_date FROM loans WHERE user_id = ? AND status = 'active'"; 
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) { 
            try (PreparedStatement pstmtQuery = conn.prepareStatement(sqlQuery)) { 
                pstmtQuery.setInt(1, userId); 
                ResultSet rs = pstmtQuery.executeQuery(); 
                while (rs.next()) { 
                    double outstandingBalance = rs.getDouble("outstanding_balance"); 
                    Date repaymentDate = rs.getDate("repayment_date"); 
                    if (outstandingBalance > 0) { 
                        System.out.println("***You have to repay RM" + outstandingBalance + " before " + repaymentDate+"***"); 
                    } 
                } 
            } 
        } catch (SQLException e) { 
            System.out.println("Error querying data from loans table: " + e.getMessage()); 
        }
    }

    
    private static boolean hasActiveLoan(int userId) { 
        String sqlQuery = "SELECT outstanding_balance FROM loans WHERE user_id = ? AND status = 'active'";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) { 
            try (PreparedStatement pstmtQuery = conn.prepareStatement(sqlQuery)) { pstmtQuery.setInt(1, userId);
            ResultSet rs = pstmtQuery.executeQuery(); 
            if (rs.next()) { 
                double outstandingBalance = rs.getDouble("outstanding_balance");
                if (outstandingBalance > 0) { 
                    return true;// User has an active loan with an outstanding balance 
                } 
            } 
            } 
        } 
        catch (SQLException e) { 
            System.out.println("Error querying loans table: " + e.getMessage()); 
        } 
        return false; 
    }

    public static void creditLoan(int userId) {
        System.out.println("== Credit Loan ==");
        System.out.println("1. Apply");
        System.out.println("2. Repay");
        System.out.print("> ");
        int choiceLoan= scanner.nextInt();
        switch (choiceLoan){
            case 1:
                // Check if the user already has an active loan 
                if (hasActiveLoan(userId)) { 
                    System.out.println("You have an existing loan that is not fully paid off. Please repay it before applying for a new loan."); 
                    return; 
                }
                
                System.out.println("== APPLY =="); 
                System.out.print("Enter the principal amount: "); 
                double principalAmount = scanner.nextDouble(); 
                System.out.print("Enter the interest rate (%): "); 
                double interestRate = scanner.nextDouble(); 
                System.out.print("Enter the repayment period (months): "); 
                int repaymentPeriod = scanner.nextInt(); 
                scanner.nextLine(); 
                if (principalAmount <= 0 || interestRate <= 0 || repaymentPeriod <= 0) { 
                    System.out.println("Invalid input values. All values must be positive."); 
                    return; 
                } 
                String status = "active"; 
                double totalRepayment = principalAmount * (1 + (interestRate / 100)); 
                double monthlyRepayment = totalRepayment / repaymentPeriod; 
                System.out.printf("Your total repayment amount is RM%.2f\n",totalRepayment);
                System.out.printf("Your monthly payment is RM%.2f\n", monthlyRepayment); 

                LocalDate currentDate = LocalDate.now(); 
                LocalDate repaymentDate = currentDate.plusMonths(repaymentPeriod); 
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy"); 
                String formattedDate = repaymentDate.format(formatter); 
                System.out.println("The loan must be paid by " + formattedDate); 
                 double outstandingBalance = totalRepayment; 
                String sqlLoan = "INSERT INTO loans (user_id, principal_amount, interest_rate, repayment_period, outstanding_balance, status, created_at,repayment_date) VALUES (?, ?, ?, ?, ?, ?, CURRENT_DATE,?)"; 

                try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) { 
                    try (PreparedStatement pstmtLoan = conn.prepareStatement(sqlLoan)) { 
                        pstmtLoan.setInt(1, userId); 
                        pstmtLoan.setDouble(2, principalAmount); 
                        pstmtLoan.setDouble(3, interestRate); 
                        pstmtLoan.setInt(4, repaymentPeriod); 
                        pstmtLoan.setDouble(5, outstandingBalance); 
                        pstmtLoan.setString(6, status); 
                          pstmtLoan.setDate(7,java.sql.Date.valueOf(repaymentDate));
                        int rowsInserted = pstmtLoan.executeUpdate(); 
                        if (rowsInserted > 0) { 
                            System.out.println("A new loan record was inserted successfully!"); 
                        } 
                    } 
                } 
                catch (SQLException e) { 
                    System.out.println("Error inserting data into loans table: " + e.getMessage()); 
                } 
                break;
                
            case 2:
                System.out.println("== REPAY =="); 
                String sqlQuery = "SELECT outstanding_balance, repayment_period, created_at FROM loans WHERE user_id = ? AND status = 'active'"; 
                try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) { 
                    try (PreparedStatement pstmtQuery = conn.prepareStatement(sqlQuery)) { 
                        pstmtQuery.setInt(1, userId); ResultSet rs = pstmtQuery.executeQuery(); 
                        if (rs.next()) { 
                            outstandingBalance = rs.getDouble("outstanding_balance"); 
                            
                            System.out.println("The money you owe: " + outstandingBalance); 
                            System.out.print("Enter the amount you wish to repay: "); 
                            double repaymentAmount = scanner.nextDouble(); 
                            if (repaymentAmount > outstandingBalance || repaymentAmount <= 0) { 
                                System.out.println("Invalid repayment amount."); 
                                return; 
                            } 
                            outstandingBalance -= repaymentAmount; 
                            loanRepaymentHistory(userId, repaymentAmount);
                            System.out.println("Repayment of RM" + repaymentAmount + " successful. Outstanding balance: " + outstandingBalance); 
                            String sqlUpdate = "UPDATE loans SET outstanding_balance = ?, status = ? WHERE user_id = ? AND status = 'active'"; 
                            try (PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdate)) {
                                pstmtUpdate.setDouble(1, outstandingBalance); 
                                pstmtUpdate.setString(2, outstandingBalance == 0 ? "repaid" : "active"); 
                                pstmtUpdate.setInt(3, userId); 
                                int rowsUpdated = pstmtUpdate.executeUpdate(); 
                                if (rowsUpdated > 0) { 
                                    System.out.println("Loan record updated successfully!"); 
                                } 
                            } 
                        } 
                        else { 
                            System.out.println("No active loan found for this user."); 
                        } 
                    } 
                } 
                catch (SQLException e) { 
                    System.out.println("Error querying data from loans table: " + e.getMessage()); 
                } 
                break;
            default:
                System.out.println("Invalid choice");
                break;
    
        }   
    }
    
    private static void loanRepaymentHistory(int userId, double repaymentAmount) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String get_loan_id = "SELECT loan_id FROM loans WHERE user_id = ? AND status = 'active' LIMIT 1";
            String insert_history = "INSERT INTO loans_repayment_history (loan_id, amount, date, user_id) VALUES (?, ?, CURRENT_TIMESTAMP, ?)";
            
            PreparedStatement getLoanIdStmt = connection.prepareStatement(get_loan_id);
            PreparedStatement insertHistoryStmt = connection.prepareStatement(insert_history);
        
             getLoanIdStmt.setInt(1, userId);
            ResultSet rs1 = getLoanIdStmt.executeQuery();

            if (rs1.next()) { // Check if there's a result
                int loanId = rs1.getInt("loan_id"); // Retrieve the loan_id
                // Insert into loan_history
                insertHistoryStmt.setInt(1, loanId);
                insertHistoryStmt.setDouble(2, repaymentAmount);
                insertHistoryStmt.setInt(3, userId);
                insertHistoryStmt.executeUpdate();
            }
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


}
