/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package latestledgersystem;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class VisualizationApp {
    private static final String URL = Constants.getDatabaseURL();
    private static final String USER = Constants.getDatabasaeUSER();
    private static final String PASSWORD = Constants.getDatabasePASSWORD();

    private int userId;

    public VisualizationApp(int userId) {
        this.userId = userId;
    }

    public void visualizeDataWithCharts() {
        // Fetch data from the database
        Map<String, Double> spendingData = fetchSpendingData();
        Map<String, Double> savingsData = fetchSavingsData();
        Map<String, Double[]> loanRepaymentData = fetchLoanRepaymentData();
        Map<String, Double> creditData = fetchCreditData();

        // Create the Swing GUI
        JFrame frame = new JFrame("Data Visualization");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new GridLayout(2, 2)); // 2x2 grid for charts

        // Create a bar chart for spending
        JFreeChart spendingChart = createPieChart3D("Debit Transactions by Category", spendingData);
        ChartPanel spendingPanel = new ChartPanel(spendingChart);
        frame.add(spendingPanel);

        // Create a pie chart for credit transactions
        JFreeChart creditChart = createPieChart3D("Credit Transactions by Category", creditData);
        ChartPanel creditPanel = new ChartPanel(creditChart);
        frame.add(creditPanel);

        // Create a line chart for savings growth
        JFreeChart savingsChart = createLineChart("Savings Growth Over Time", "Date", "Amount (RM)", savingsData);
        ChartPanel savingsPanel = new ChartPanel(savingsChart);
        frame.add(savingsPanel);

        // Create a bar chart for loan repayment
        JFreeChart loanRepaymentChart = createLoanRepaymentChart("Loan Repayment Progress", "Date", "Amount (RM)", loanRepaymentData);
        ChartPanel loanRepaymentPanel = new ChartPanel(loanRepaymentChart);
        frame.add(loanRepaymentPanel);

        // Display the frame
        frame.pack();
        frame.setLocationRelativeTo(null); // Center the frame
        frame.setVisible(true);
    }

    // Helper method to create a bar chart (not used because bar chart is whack)
    private JFreeChart createBarChart(String title, String categoryAxisLabel, String valueAxisLabel, Map<String, Double> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            dataset.addValue(entry.getValue(), entry.getKey(), "");
        }

        return ChartFactory.createBarChart(
                title,
                categoryAxisLabel,
                valueAxisLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);
    }
    
    private JFreeChart createPieChart3D(String title, Map<String, Double> data) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        return ChartFactory.createPieChart3D(
                title,
                dataset,
                true, // include legend
                true, // include tooltips
                false // no URLs
        );
    }

    // Helper method to create a line chart
    private JFreeChart createLineChart(String title, String xAxisLabel, String yAxisLabel, Map<String, Double> data) {
        XYSeries series = new XYSeries("Savings");

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            String dateTimeStr = entry.getKey();
            double amount = entry.getValue();

            // Parse datetime string into LocalDateTime with second precision
            java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(dateTimeStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Convert LocalDateTime to milliseconds since epoch
            long milliseconds = dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

            // Add to the series
            series.add(milliseconds, amount);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        // Create the chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                xAxisLabel,
                yAxisLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        // Customize the X-axis to display dates with second precision
        org.jfree.chart.axis.DateAxis xAxis = new org.jfree.chart.axis.DateAxis("Date");
        xAxis.setDateFormatOverride(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        chart.getXYPlot().setDomainAxis(xAxis);

        return chart;
    }
    
    private JFreeChart createLoanRepaymentChart(String title, String xAxisLabel, String yAxisLabel, Map<String, Double[]> data) {
        XYSeries repaymentSeries = new XYSeries("Repayment Amount");
        XYSeries remainingBalanceSeries = new XYSeries("Remaining Balance");

        for (Map.Entry<String, Double[]> entry : data.entrySet()) {
            String dateTimeStr = entry.getKey();
            double repaymentAmount = entry.getValue()[0];
            double remainingBalance = entry.getValue()[1];

            // Parse datetime string into LocalDateTime with second precision
            java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(dateTimeStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Convert LocalDateTime to milliseconds since epoch
            long milliseconds = dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

            // Add to the series
            repaymentSeries.add(milliseconds, repaymentAmount);
            remainingBalanceSeries.add(milliseconds, remainingBalance);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(repaymentSeries);
        dataset.addSeries(remainingBalanceSeries);

        // Create the chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                xAxisLabel,
                yAxisLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        // Customize the X-axis to display dates with second precision
        org.jfree.chart.axis.DateAxis xAxis = new org.jfree.chart.axis.DateAxis("Date");
        xAxis.setDateFormatOverride(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        chart.getXYPlot().setDomainAxis(xAxis);

        return chart;
    }
    
    // Fetch spending data from the database
    private Map<String, Double> fetchSpendingData() {
        Map<String, Double> spendingData = new HashMap<>();
        String query = "SELECT description, SUM(amount) as total FROM transactions WHERE user_id = ? AND transaction_type = 'debit' GROUP BY description";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String description = rs.getString("description");
                double total = rs.getDouble("total");
                spendingData.put(description, total);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching spending data: " + e.getMessage());
        }

        return spendingData;
    }
    
    private Map<String, Double> fetchCreditData() {
        Map<String, Double> creditData = new HashMap<>();
        String query = "SELECT description, SUM(amount) as total FROM transactions WHERE user_id = ? AND transaction_type = 'credit' GROUP BY description";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String description = rs.getString("description");
                double total = rs.getDouble("total");
                creditData.put(description, total);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching spending data: " + e.getMessage());
        }

        return creditData;
    }   
    

    // Fetch savings data from the database
    private Map<String, Double> fetchSavingsData() {
        Map<String, Double> savingsData = new HashMap<>();
        String query = "SELECT date, amount FROM savings_history WHERE user_id = ? ORDER BY date";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            double total = 0;
            while (rs.next()) {
                // Fetch datetime as a string
                String dateTimeStr = rs.getString("date");
                double amount = rs.getDouble("amount");

                // Parse datetime string into LocalDateTime with second precision
                java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(dateTimeStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                // Format datetime for the chart (e.g., "yyyy-MM-dd HH:mm:ss")
                String formattedDateTime = dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                // Add to the savingsData map
                total += amount;
                savingsData.put(formattedDateTime, total);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching savings data: " + e.getMessage());
        }

        return savingsData;
    }
    

    private Map<String, Double[]> fetchLoanRepaymentData() {
        Map<String, Double[]> loanRepaymentData = new HashMap<>();
        String repaymentQuery = "SELECT date, amount FROM loans_repayment_history WHERE user_id = ? AND loan_id = ? ORDER BY date";
        String loanQuery = "SELECT loan_id, principal_amount, interest_rate, outstanding_balance FROM loans WHERE user_id = ? AND status = 'active'";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement repaymentStmt = conn.prepareStatement(repaymentQuery);
             PreparedStatement loanStmt = conn.prepareStatement(loanQuery)) {

            // Fetch loan details
            loanStmt.setInt(1, userId);
            ResultSet loanRs = loanStmt.executeQuery();

            int loan_id = 0;
            double principal_amount = 0;
            double interest_rate = 0;
            double remainingBalance;

            if (loanRs.next()) {
                loan_id = loanRs.getInt("loan_id");
                principal_amount = loanRs.getDouble("principal_amount");
                interest_rate = loanRs.getDouble("interest_rate");
                remainingBalance = loanRs.getDouble("outstanding_balance");
            }

            // Fetch repayment history
            repaymentStmt.setInt(1, userId);
            repaymentStmt.setInt(2, loan_id);
            ResultSet repaymentRs = repaymentStmt.executeQuery();

            // Calculate total loan amount
            double totalLoanAmount = principal_amount * (1 + (interest_rate / 100));

            while (repaymentRs.next()) {
                // Fetch datetime as a string
                String dateTimeStr = repaymentRs.getString("date");
                double amount = repaymentRs.getDouble("amount");

                // Parse datetime string into LocalDateTime with second precision
                java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(dateTimeStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                // Format datetime for the chart (e.g., "yyyy-MM-dd HH:mm:ss")
                String formattedDateTime = dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                // Calculate remaining balance
                totalLoanAmount -= amount;

                // Store repayment amount and remaining balance for each datetime
                loanRepaymentData.put(formattedDateTime, new Double[]{amount, totalLoanAmount});
            }

        } catch (SQLException e) {
            System.out.println("Error fetching loan repayment data: " + e.getMessage());
        }

        return loanRepaymentData;
    }
}
