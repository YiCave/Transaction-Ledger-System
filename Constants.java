/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package latestledgersystem;

/**
 *
 * @author user
 */
public class Constants {
   private static final String URL = "jdbc:mysql://localhost:3306/ledgersystem";
    private static final String USER = "root"; // your MySQL username
    private static final String PASSWORD = "";  // your MySQL password

    public static String getDatabaseURL() {
        return URL;
    }

    public static String getDatabasaeUSER() {
        return USER;
    }

    public static String getDatabasePASSWORD() {
        return PASSWORD;
    }
  
}
