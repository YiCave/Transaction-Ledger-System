/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package latestledgersystem;

import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 *
 * @author justb
 */
public class Utils {
    private static final Scanner scanner = new Scanner(System.in);
    
    public static void exportToCSV(String csvContent) {
        System.out.print("Enter the file name for the CSV (e.g., history.csv): ");
        String fileName = scanner.nextLine();

        File file = new File(fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(csvContent);
            System.out.println("History successfully exported to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error exporting to CSV: " + e.getMessage());
        }
    }

    public static String[] wrapText(String text, int width) {
        if (text == null || text.isEmpty()) {
            return new String[] { "" };
        }

        StringBuilder currentLine = new StringBuilder();
        StringBuilder wrappedText = new StringBuilder();
        for (String word : text.split(" ")) {
            if (currentLine.length() + word.length() + 1 <= width) {
                currentLine.append((currentLine.length() > 0 ? " " : "")).append(word);
            } else {
                wrappedText.append(currentLine).append("\n");
                currentLine.setLength(0); // Clear the current line
                currentLine.append(word);
            }
        }
        wrappedText.append(currentLine); // Append the last line

        return wrappedText.toString().split("\n");
    }
    
    public static String promptForValidName() {
        String name;
        while (true) {
            System.out.print("Name (alphanumeric and spaces only): ");
            name = scanner.nextLine();
            if (name.matches("^[a-zA-Z0-9 ]+$")) {
                break;
            } else {
                System.out.println("Invalid name. Please use alphanumeric characters and spaces only.");
            }
        }
        return name;
    }

    protected static String promptForValidEmail() {
        String email;
        while (true) {
            System.out.print("Email: ");
            email = scanner.nextLine();
            if (Pattern.matches("^[\\w-\\.]+@[\\w-]+\\.[a-z]{2,}$", email)) {
                break;
            } else {
                System.out.println("Invalid email format. Please enter a valid email (e.g., name@example.com).");
            }
        }
        return email;
    }

    public static String promptForValidPassword() {
        String password;
        while (true) {
            System.out.print("Password (min 6 chars, at least 1 special char): ");
            password = scanner.nextLine();
            System.out.print("Confirm Password: ");
            String confirmPassword = scanner.nextLine();
            if (password.length() >= 6 && password.matches(".*[!@#$%^&*(),.?\":{}|<>].*") && password.equals(confirmPassword)) {
                return password;
            } else if (!password.equals(confirmPassword)) {
                System.out.println("Passwords do not match.");
            } else {
                System.out.println("Password does not meet complexity requirements.");
            }
        }
    }
    
    public static double promptForPositiveAmount() {
        double amount;
        while (true) {
            System.out.print("Enter amount: ");
            amount = scanner.nextDouble();
            scanner.nextLine(); // Consume newline
            if (amount > 0) {
                return amount; // Valid amount
            } else {
                System.out.println("Amount must be positive.");
            }
        }
    }

    public static String promptForDescription() {
        String description;
        while (true) {
            System.out.print("Enter description (max 100 chars): ");
            description = scanner.nextLine();
            if (description.length() <= 100) {
                return description; // Valid description
            } else {
                System.out.println("Description cannot exceed 100 characters.");
            }
        }
    }
    
    public static boolean isPasswordHashed(String password) {
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }
    
}
