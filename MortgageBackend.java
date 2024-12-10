import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MortgageBackend {
    private static final String DB_URL = "jdbc:postgresql://127.0.0.1:5432/mortgage_applications";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "2580";

    private static List<String> filters = new ArrayList<>();

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Connected to the database!");

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("\nMain Menu:");
                System.out.println("1. Search for Mortgages");
                System.out.println("2. Calculate Rate");
                System.out.println("3. Package Mortgages");
                System.out.println("4. Exit");

                int choice = getIntInput(scanner, "Enter your choice: ");

                switch (choice) {
                    case 1:
                        searchMortgages(scanner, conn);
                        break;
                    case 2:
                        calculateRate(conn);
                        break;
                    case 3:
                        packageMortgages(conn);
                        break;
                    case 4:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void searchMortgages(Scanner scanner, Connection conn) {
        while (true) {
            System.out.println("\nSearch Menu:");
            System.out.println("1. Add Filter");
            System.out.println("2. Delete Filter");
            System.out.println("3. View Current Filters");
            System.out.println("4. Exit Search");

            int choice = getIntInput(scanner, "Enter your choice: ");

            switch (choice) {
                case 1:
                    addFilter(scanner, conn);
                    break;
                case 2:
                    deleteFilter(scanner);
                    break;
                case 3:
                    System.out.println("Current Filters: " + filters);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void addFilter(Scanner scanner, Connection conn) {
        System.out.println("Choose a filter type:");
        System.out.println("1. County");
        System.out.println("2. Loan Type");
        System.out.println("3. Income-to-Debt Ratio");
        System.out.println("4. MSAMD");
        System.out.println("5. Tract to MSAMD Income ");
        System.out.println("6. Loan Purpose");
        System.out.println("7. Property Type");
        int filterType = getIntInput(scanner, "Enter your choice: ");

        switch (filterType) {
            case 1:
                listCounties(conn);
                System.out.print("Enter the county name: ");
                String countyName = scanner.nextLine().trim();
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT county_code FROM county_mapping WHERE county_name = ?")) {
                    stmt.setString(1, countyName);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            int countyCode = rs.getInt("county_code");
                            filters.add("county_code = " + countyCode);
                            System.out.println("Filter added: County Code = " + countyCode + " (" + countyName + ")");
                        } else {
                            System.out.println("County not found. Please check the spelling or list available counties.");
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                int loanType = getIntInput(scanner, "Enter loan type: ");
                filters.add("loan_type = " + loanType);
                System.out.println("Filter added: Loan Type = " + loanType);
                break;
            case 3:
            //unsure if we should add more user clarification here 
            //user enters min and max ratio and filters are added based on the value landing in between 
                double minRatio = getDoubleInput(scanner, "Enter minimum income-to-debt ratio: ");
                double maxRatio = getDoubleInput(scanner, "Enter maximum income-to-debt ratio: ");
                filters.add("applicant_income_000s / loan_amount_000s BETWEEN " + minRatio + " AND " + maxRatio);
                System.out.println("Filter added: Income-to-Debt Ratio = " + minRatio + " to " + maxRatio);
                break;
            case 4:
                listMSAMD(conn); // Display the MSAMD list
                System.out.print("Enter the MSAMD codes (comma-separated): ");
                String input = scanner.nextLine().trim();
                String[] msamdCodes = input.split(",");
                //convert user input into filters 
                
                StringBuilder filter = new StringBuilder("msamd IN (");
                for (int i = 0; i < msamdCodes.length; i++) {
                    filter.append(msamdCodes[i].trim());
                    if (i < msamdCodes.length - 1) {
                        filter.append(", ");
                    }
                }
                filter.append(")");

                // Add the filter to the filters list
                filters.add(filter.toString());
                System.out.println("Filter added: " + filter.toString());
                break;
            case 5:
                
            
            default:
                System.out.println("Invalid filter type.");
        }
    }
    private static void listMSAMD(Connection conn){
        System.out.println("List of MSAMDs:");
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT  DISTINCT msamd_name, msamd FROM preliminary;")){
            while (rs.next()) {
                String msamd = rs.getString("msamd");
                String msamdName = rs.getString("msamd_name");
                if (msamdName == null) {
                    msamdName = "(No Name Available)";
                }
    
                System.out.println(msamd + " - " + msamdName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void listCounties(Connection conn){
        System.out.println("List of Counties:");
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT county_name FROM county_mapping;")){
            while (rs.next()) {
                String c = rs.getString("county_name");
                System.out.println(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void deleteFilter(Scanner scanner) {
        if (filters.isEmpty()) {
            System.out.println("No filters to delete.");
            return;
        }

        System.out.println("Current Filters:");
        for (int i = 0; i < filters.size(); i++) {
            System.out.println((i + 1) + ". " + filters.get(i));
        }

        int filterNumber = getIntInput(scanner, "Enter the filter number to delete (or 0 to delete all): ");
        if (filterNumber == 0) {
            filters.clear();
            System.out.println("All filters cleared.");
        } else if (filterNumber > 0 && filterNumber <= filters.size()) {
            System.out.println("Filter removed: " + filters.get(filterNumber - 1));
            filters.remove(filterNumber - 1);
        } else {
            System.out.println("Invalid filter number.");
        }
    }

    private static void calculateRate(Connection conn) {
        if (filters.isEmpty()) {
            System.out.println("No filters applied. Please add filters first.");
            return;
        }

        String query = "SELECT loan_amount_000s, rate_spread, lien_status FROM applications WHERE action_taken = 1";
        query += " AND " + String.join(" AND ", filters);

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            double totalLoanAmount = 0;
            double weightedRateSum = 0;

            while (rs.next()) {
                double loanAmount = rs.getDouble("loan_amount_000s");
                double rateSpread = rs.getDouble("rate_spread");
                int lienStatus = rs.getInt("lien_status");

                // Adjust rateSpread if unknown
                if (rs.wasNull()) {
                    rateSpread = (lienStatus == 1) ? 1.5 : 3.5;
                }

                weightedRateSum += (rateSpread + 2.33) * loanAmount;
                totalLoanAmount += loanAmount;
            }

            if (totalLoanAmount > 0) {
                double weightedRate = weightedRateSum / totalLoanAmount;
                System.out.println("Weighted Rate: " + weightedRate);
                System.out.println("Total Loan Amount: " + totalLoanAmount);
            } else {
                System.out.println("No results match the filters.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void packageMortgages(Connection conn) {
        if (filters.isEmpty()) {
            System.out.println("No filters applied. Please add filters first.");
            return;
        }

        String updateQuery = "UPDATE applications SET purchaser_type = 9 WHERE action_taken = 1";
        updateQuery += " AND " + String.join(" AND ", filters);

        try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            int updatedRows = pstmt.executeUpdate();
            if (updatedRows > 0) {
                System.out.println(updatedRows + " mortgages successfully packaged.");
            } else {
                System.out.println("No mortgages matched the filters for packaging.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int getIntInput(Scanner scanner, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    private static double getDoubleInput(Scanner scanner, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }
}
