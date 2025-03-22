/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package tacp1;
/*import java.util.Scanner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;*/

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.LocalTime;
import java.time.Duration;
import java.io.FileReader;
import java.io.IOException;
import com.opencsv.CSVReader;

/**
 *
 * @author Hp
 */
public class TACP1 {

    /**
     * @param args the command line arguments
     */
 // Base Google Sheets CSV URL
    private static final String empDETAILS_URL = "https://docs.google.com/spreadsheets/d/e/2PACX-1vRSib3-eoyhZfsNIGL0kj2cHNhcRw6efjoE63jtm4mizONxWaGfNyDFCA_e9BboKEKZOY6qhQxbUS6v/pub?output=csv";

    // GID for different sheets
    private static final String empPROFILE_GID = "1510394485"; // Default first sheet (Profile)
    private static final String empATTENDANCE_GID = "1129354034"; // Replace with actual Attendance sheet GID

    // Time Formatter
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM-dd-yyyy");
      
    // Method to check credentials
    public static boolean checkCredentials(String empNumber/*can be changed*/, String lastName/*can be changed*/) {
        try {
            String urlWithGid = empDETAILS_URL + "&gid=" + empPROFILE_GID;
            URL url = new URL(urlWithGid);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] credentials = line.split(",");
                if (credentials.length >= 4) {
                    String storedEmpNumber = credentials[0].trim(); // Column A
                    String storedLastName = credentials[1].trim(); // Column B

                    if (storedEmpNumber.equals(empNumber) && storedLastName.equalsIgnoreCase(lastName)) {
                        return true;
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            System.out.println("Error accessing Google Sheets: " + e.getMessage());
        }
        return false;
    }

    // Method to fetch and display Employee Profile
    public static void viewProfile(String empNumber) {
        try {
            String urlWithGid = empDETAILS_URL + "&gid=" + empPROFILE_GID;
            URL url = new URL(urlWithGid);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 4) {
                    String storedEmpNumber = data[0].trim(); // Column A
                    if (storedEmpNumber.equals(empNumber)) {
                        String fullName = data[2].trim() + " " + data[1].trim(); // Column C + Column B
                        String birthday = data[3].trim(); // Column D

                        System.out.println("\nPROFILE: ");
                        System.out.println("Full Name: " + fullName);
                        System.out.println("Employee Number: " + storedEmpNumber);
                        System.out.println("Birthday: " + birthday);
                        System.out.println("----------------------------");
                        break;
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            System.out.println("Error accessing Google Sheets: " + e.getMessage());
        }
    }

    // Method to fetch and display Employee Attendance
   public static void viewAttendance(String empNumber) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nATTENDANCE MENU:");
            System.out.println("\n1. Daily Attendance Records");
            System.out.println("2. Calculate Hours Worked Per Week");
            System.out.println("3. Back to Main Menu");
            System.out.println("----------------------------");
            System.out.print("\nEnter your choice: ");
            

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    listAttendance(empNumber);
                    break;
                case 2:
                    calculateWeeklyHours(empNumber);
                    break;
                case 3:
                    return; // Go back to main menu
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

       public static void listAttendance(String empNumber) {
        try {
            String urlWithGid = empDETAILS_URL + "&gid=" + empATTENDANCE_GID;
            URL url = new URL(urlWithGid);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            br.readLine(); // Skip header

            boolean attendanceFound = false;
            System.out.println("ATTENDANCE RECORDS:");
            System.out.println("===================================================");
         
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 6) { // Ensuring required columns exist
                    String storedEmpNumber = data[0].trim(); // Column A
                    if (storedEmpNumber.equals(empNumber)) {
                        String date = data[3].trim(); // Column B
                        String status = data[0].trim(); // Column A
                        String logInTime = data[4].trim(); // Column E
                        String logOutTime = data[5].trim(); // Column F

                        // Calculate hours worked
                        double hoursWorked = calculateHoursWorked(logInTime, logOutTime);

                        System.out.println("Date: " + date + " | Employee #: " + status + " | Hours Worked: " + hoursWorked);
                        attendanceFound = true;
                    }
                }
            }

            if (!attendanceFound) {
                System.out.println("No attendance records found for Employee #" + empNumber);
            }

            System.out.println("===================================================");
            br.close();
        } catch (Exception e) {
            System.out.println("Error accessing Google Sheets: " + e.getMessage());
        }
    }




    // Method to calculate hours worked
    private static double calculateHoursWorked(String logIn, String logOut) {
    try {
        if (!logIn.isEmpty() && !logOut.isEmpty()) {
            LocalTime inTime = LocalTime.parse(logIn, TIME_FORMAT);
            LocalTime outTime = LocalTime.parse(logOut, TIME_FORMAT);

            if (outTime.isBefore(inTime)) {
                System.out.println("Error: Logout time is before login time.");
                return 0.0;
            }

            Duration duration = Duration.between(inTime, outTime);
            return duration.toMinutes() / 60.0 - 1; // Deduct 1 hour
        }
    } catch (Exception e) {
        System.out.println("Error parsing time: " + e.getMessage());
    }
    return 0.0;
}
    
 
    public static void calculateWeeklyHours(String empNumber) {
    try {
        String urlWithGid = empDETAILS_URL + "&gid=" + empATTENDANCE_GID;
        HttpURLConnection conn = (HttpURLConnection) new URL(urlWithGid).openConnection();
        conn.setRequestMethod("GET");

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        br.readLine(); // Skip header

        Map<Integer, Double> weeklyHours = new HashMap<>();
        String line;
        
        while ((line = br.readLine()) != null) { // Corrected loop
            String[] data = line.split(",");
            if (data.length >= 6 && data[0].trim().equals(empNumber)) {
                String dateString = data[3].trim(); // Column D
                String logInTime = data[4].trim(); // Column E
                String logOutTime = data[5].trim(); // Column F

                try {
                    LocalDate date = LocalDate.parse(dateString, DATE_FORMAT);
                    int weekOfYear = date.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
                    double hoursWorked = calculateHoursWorked(logInTime, logOutTime);

                    weeklyHours.put(weekOfYear, weeklyHours.getOrDefault(weekOfYear, 0.0) + hoursWorked);
                } catch (Exception e) {
                    System.out.println("Error parsing date: " + dateString);
                }
            }
        }
        br.close();

        if (weeklyHours.isEmpty()) {
            System.out.println("No weekly hours data found.");
            return;
        }

        System.out.println("WEEK # | TOTAL HOURS WORKED");
        System.out.println("------------------------------------");
        for (Map.Entry<Integer, Double> entry : weeklyHours.entrySet()) {
            System.out.printf("Week %d | %.2f hours\n", entry.getKey(), entry.getValue());
        }

    } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
    }
}

public static void viewPayroll(String empNumber) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nPAYROLL MENU:");
            System.out.println("\n1. SALARY");
            System.out.println("2. DEDUCTIONS");
            System.out.println("3. Back to Main Menu");
            System.out.println("----------------------------");
            System.out.print("\nEnter your choice: ");
            

             int choice = scanner.nextInt();
             scanner.nextLine(); // Consume newline

              switch (choice) {
                case 1:
                    calculateSalary(empNumber);
                    break;
                case 2:
                    calculateDeductions(empNumber);
                    break;
                case 3:
                    return; // Go back to main menu
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

public static void calculateSalary(String empNumber) {
    try {
        // Use empPROFILE_GID to fetch the salary data from the Profile sheet
        String urlWithGid = empDETAILS_URL + "&gid=" + empPROFILE_GID;
        HttpURLConnection conn = (HttpURLConnection) new URL(urlWithGid).openConnection();
        conn.setRequestMethod("GET");

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        br.readLine(); // Skip header

        String salaryStr = "5,000";
        double salary = 5000.0;
        boolean salaryFound = false;

        while ((salaryStr = br.readLine()) != null) {
            String[] data = salaryStr.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            
            if (data.length >= 18 && data[0].trim().equals(empNumber)) { // Ensure sufficient columns
                try {
                    salaryStr = data[13].trim().replaceAll("[\",]", "").trim(); // Column 14 (Index 13)
                    
                    //other compensation
                    String riceSubsidy = data[14].trim().replaceAll("[\",]", "");
                    String phoneAllowance = data[15].trim().replaceAll("[\",]", "");
                    String clothingAllowance = data[16].trim().replaceAll("[\",]", "");
                    String grossSemiMonthlyRate = data[17].trim().replaceAll("[\",]", "");
                    String hourlyRate = data[18].trim().replaceAll("[\",]", "");
                    
                    // Convert to double AFTER cleaning
                    salary = Double.parseDouble(salaryStr);                    
                    System.out.println("Salary: " + salary);
                    
                    salaryFound = true;
                    
                    // Print Additional Data
                    System.out.println("Rice Subsidy: PHP " + riceSubsidy);
                    System.out.println("Phone Allowance: PHP " + phoneAllowance);
                    System.out.println("Clothing Allowance: PHP " + clothingAllowance);
                    System.out.println("Gross Semi-Monthly Rate: PHP " + grossSemiMonthlyRate);
                    System.out.println("Hourly Rate: PHP " + hourlyRate);
                    
                    
                    return; 
                    
                } catch (NumberFormatException e) {
                    System.out.println("Error: Invalid number format in salary data. " + e.getMessage());
                    return;
                }
            }
        }
        
        if (!salaryFound) {
            System.out.println("Error: No salary data found for Employee #" + empNumber);
}
        
        //method to get deductions 
        //LINE AFTER THIS ARE STILL INPROGRESS
        
        // Step 2: Compute Hourly Rate (column 14 / 21) / 8
        double hourlyRate = (Double.parseDouble(salaryStr) / 21) / 8 ;

        // Step 3: Get Total Hours Worked
        double totalHoursWorked = getTotalHoursWorked(empNumber);

        // Step 4: Compute Salary
        double computedSalary = totalHoursWorked * hourlyRate;

        // Output
        System.out.printf("Computed Salary for Employee #%s: PHP %.2f%n", empNumber, computedSalary);
        System.out.println("Computed Hourly Rate: " + hourlyRate);
        System.out.println("Total Hours Worked: " + totalHoursWorked);
        System.out.println("Final Computed Salary: " + computedSalary);
        System.out.println("----------------------------");

    } catch (Exception e) {
        System.out.println("Error calculating salary: " + e.getMessage());
    }
   
}

public static double getTotalHoursWorked(String empNumber) {
    double totalHours = 0.0;
    try {
        String urlWithGid = empDETAILS_URL + "&gid=" + empATTENDANCE_GID;
        HttpURLConnection conn = (HttpURLConnection) new URL(urlWithGid).openConnection();
        conn.setRequestMethod("GET");

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        br.readLine(); // Skip header row

        String line;
        while ((line = br.readLine()) != null) {
            String[] data = line.split(",");
            if (data.length >= 6 && data[0].trim().equals(empNumber)) {
                String logInTime = data[4].trim();  // Column 5 (Index 4)
                String logOutTime = data[5].trim(); // Column 6 (Index 5)

                double hoursWorked = calculateHoursWorked(logInTime, logOutTime);
                totalHours += hoursWorked;
            }
        }
        br.close();
    } catch (Exception e) {
        System.out.println("Error fetching attendance: " + e.getMessage());
    }
    return totalHours;
}





private static void calculateDeductions(String empNumber) {
    double salary = 0;
    double taxRate = 0.1; // Example 10% tax
    double deductions = salary * taxRate;

    System.out.printf("Total Deductions: $%.2f\n", deductions);
}



    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Login Prompt
        System.out.print("Username (Employee Number): ");
        String empNumber = scanner.nextLine().trim();

        System.out.print("Password (Last Name): ");
        String lastName = scanner.nextLine().trim();

        // Authentication
        if (checkCredentials(empNumber, lastName)) {
            System.out.println("\nLogin successful! Welcome, Employee #" + empNumber + ".");

            while (true) {
                System.out.println("\nPlease choose an option:\n");
                System.out.println("1. View Profile");
                System.out.println("2. View Attendance");
                System.out.println("3. View Payroll");
                System.out.println("4. Logout");
                System.out.println("----------------------------");
                System.out.print("\nEnter your choice: ");
                

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        viewProfile(empNumber);
                        break;
                    case 2:
                        viewAttendance(empNumber);
                        break;
                    case 3: 
                        viewPayroll(empNumber);
                        break;
                    case 4:
                        System.out.println("LOGGING OUT...");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } else {
            System.out.println("Invalid Employee Number or Last Name.");
        }

        scanner.close();
    } 
}
