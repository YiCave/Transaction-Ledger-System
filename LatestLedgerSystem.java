/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package latestledgersystem;

import java.util.Scanner;

/**
 *
 * @author user
 */
public class LatestLedgerSystem {

   private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean isLoggedIn = false;
        //encrypt password that are unecrypted
        AuthenticationManager.updateUnencryptedPasswords();
        while (!isLoggedIn) {
            System.out.println("== Ledger System ==");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("> ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (choice == 1) 
                isLoggedIn = AuthenticationManager.login();
            else if (choice == 2) 
                AuthenticationManager.register();
            else if (choice == 3){
                System.out.println("Program Ending.... See you next time! :) ");
                break;
            }           
            else 
                System.out.println("Invalid choice.");
        }
    }

    
}
