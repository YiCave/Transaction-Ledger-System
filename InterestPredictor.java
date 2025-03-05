/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package latestledgersystem;

import java.util.Scanner;

/**
 *
 * @author justb
 */
public class InterestPredictor {
    public static void depositInterestPredictor(int userId) {
        Scanner sc=new Scanner(System.in);
        System.out.println("Available Banks and Interest Rates:");
        System.out.println("1. Maybank - 2.5%");
        System.out.println("2. CIMB Group - 2.45%");
        System.out.println("3. Public Bank - 3.65%");
        System.out.println("4. RHB Bank - 2.3%");
        System.out.println("5. Hong Leong Bank - 2.3%");
        System.out.println("6. AmBank - 2.45%");
        System.out.println("7. United Overseas Bank (Malaysia) - 2.7%");
        System.out.println("8. HSBC Bank Malaysia - 2.45%");
        System.out.println("9. Bank Islam - 4.0%");

        double interestRate = 0.0;
        while (true){
            System.out.println("\nPlease select a bank (from 1-9): ");
            int bank = sc.nextInt();

            switch (bank) {
                case 1:
                    interestRate = 2.5;
                    break;
                case 2:
                    interestRate = 2.45;
                    break;
                case 3:
                    interestRate = 3.65;
                    break;
                case 4:
                    interestRate = 2.3;
                    break;
                case 5:
                    interestRate = 2.3;
                    break;
                case 6:
                    interestRate = 2.45;
                    break;
                case 7:
                    interestRate = 2.7;
                    break;
                case 8:
                    interestRate = 2.45;
                    break;
                case 9:
                    interestRate = 4.0;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    continue; // Restart loop if invalid input
                }
                break; // Exit loop if valid choice is made
            }


        // Get deposit amount
        double depositAmount;

        while (true) {
            System.out.print("Enter your deposit amount: RM ");
            depositAmount = sc.nextDouble();
            if (depositAmount > 0) {
                break; // Exit loop if valid input
            } else {
                System.out.println("Deposit amount must be greater than zero. Please try again.");
            }
        }

    // Get calculation period
        int months;
        while (true) {
            System.out.print("Enter the number of months for interest calculation: ");
            months = sc.nextInt();
            if (months > 0) {
                break; // Exit loop if valid input
            } else {
                System.out.println("Months must be greater than zero. Please try again.");
            }
        }

        // Calculate interest
        double monthlyRate = interestRate / 12 / 100; // Convert annual rate to monthly decimal
        double totalInterest = depositAmount * monthlyRate * months;
        double totalBalance = depositAmount + totalInterest;

        System.out.println("\nResult:");
        System.out.printf("Bank: %.2f%% Annual Interest Rate\n", interestRate);
        System.out.printf("Deposit Amount: RM%.2f\n", depositAmount);
        System.out.printf("Total Interest Earned: RM%.2f\n", totalInterest);
        System.out.printf("Total Balance after %d months: RM%.2f\n", months, totalBalance);

    }
}

