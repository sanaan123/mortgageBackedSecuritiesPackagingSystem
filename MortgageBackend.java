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
                // Display active filters and matching results
                System.out.println("\nActive Filters: " + getReadableFilters());
                displayMatchingRowsAndLoanAmount(conn);
            
                // Main menu
                System.out.println("\nMain Menu:");
                System.out.println("1. Search for Mortgages (Add/Delete Filters)");
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
        System.out.println("3. Income to Debt Ratio");
        System.out.println("4. MSAMD");
        System.out.println("5. Tract to MSAMD Income");
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
                // User enters min and max ratio, filters are added based on the values provided
                System.out.print("Enter minimum income-to-debt ratio (or press Enter to skip): ");
                String minRatioInput = scanner.nextLine().trim();
                System.out.print("Enter maximum income-to-debt ratio (or press Enter to skip): ");
                String maxRatioInput = scanner.nextLine().trim();
                if (minRatioInput.isEmpty()) {
                    filters.add("applicant_income_000s / loan_amount_000s <= " + maxRatioInput);
                    System.out.println("Filter added: Income-to-Debt Ratio <= " + maxRatioInput);
                } else if (maxRatioInput.isEmpty()) {
                    filters.add("applicant_income_000s / loan_amount_000s >= " + minRatioInput);
                    System.out.println("Filter added: Income-to-Debt Ratio >= " + minRatioInput);
                } else {
                    filters.add("applicant_income_000s / loan_amount_000s BETWEEN " + minRatioInput + " AND " + maxRatioInput);
                    System.out.println("Filter added: Income-to-Debt Ratio = " + minRatioInput + " to " + maxRatioInput);
                }
                break;
            case 4:
                listMSAMD(conn);
                System.out.print("Enter the MSAMD codes (comma-separated): ");
                String input = scanner.nextLine().trim();
                String[] msamdCodes = input.split(",");
                StringBuilder filter = new StringBuilder("msamd IN (");
                for (int i = 0; i < msamdCodes.length; i++) {
                    filter.append(msamdCodes[i].trim());
                    if (i < msamdCodes.length - 1) {
                        filter.append(", ");
                    }
                }
                filter.append(")");
                filters.add(filter.toString());
                System.out.println("Filter added: " + filter.toString());
                break;
            case 5:
                // Handle tract_to_msamd_income directly from the preliminary table
                System.out.print("Enter minimum tract-to-msamd income (or press Enter to skip): ");
                String minTractIncomeInput = scanner.nextLine().trim();
                System.out.print("Enter maximum tract-to-msamd income (or press Enter to skip): ");
                String maxTractIncomeInput = scanner.nextLine().trim();
            
                StringBuilder preliminaryFilter = new StringBuilder();
                if (!minTractIncomeInput.isEmpty() && !maxTractIncomeInput.isEmpty()) {
                    preliminaryFilter.append("tract_to_msamd_income BETWEEN ").append(minTractIncomeInput).append(" AND ").append(maxTractIncomeInput);
                    System.out.println("Filter added: Tract-to-MSAMD Income = " + minTractIncomeInput + " to " + maxTractIncomeInput);
                } else if (!minTractIncomeInput.isEmpty()) {
                    preliminaryFilter.append("tract_to_msamd_income >= ").append(minTractIncomeInput);
                    System.out.println("Filter added: Tract-to-MSAMD Income >= " + minTractIncomeInput);
                } else if (!maxTractIncomeInput.isEmpty()) {
                    preliminaryFilter.append("tract_to_msamd_income <= ").append(maxTractIncomeInput);
                    System.out.println("Filter added: Tract-to-MSAMD Income <= " + maxTractIncomeInput);
                }
            
                if (preliminaryFilter.length() > 0) {
                    filters.add(preliminaryFilter.toString());
                }
                break;
            case 6:
                System.out.println("Enter loan purpose types (comma-separated): ");
                String loanPurposeInput = scanner.nextLine().trim();
                String[] loanPurposeTypes = loanPurposeInput.split(",");
                StringBuilder loanPurposeFilter = new StringBuilder("loan_purpose IN (");
                for (int i = 0; i < loanPurposeTypes.length; i++) {
                    loanPurposeFilter.append(loanPurposeTypes[i].trim());
                    if (i < loanPurposeTypes.length - 1) {
                        loanPurposeFilter.append(", ");
                    }
                }
                loanPurposeFilter.append(")");
                filters.add(loanPurposeFilter.toString());
                System.out.println("Filter added: " + loanPurposeFilter.toString());
                break;
            case 7:
                System.out.println("Enter property types (comma-separated): ");
                String propertyTypeInput = scanner.nextLine().trim();
                String[] propertyTypes = propertyTypeInput.split(",");
                StringBuilder propertyTypeFilter = new StringBuilder("property_type IN (");
                for (int i = 0; i < propertyTypes.length; i++) {
                    propertyTypeFilter.append(propertyTypes[i].trim());
                    if (i < propertyTypes.length - 1) {
                        propertyTypeFilter.append(", ");
                    }
                }
                propertyTypeFilter.append(")");
                filters.add(propertyTypeFilter.toString());
                System.out.println("Filter added: " + propertyTypeFilter.toString());
                break;
            default:
                System.out.println("Invalid filter type.");
        }
    }
    
    private static void listMSAMD(Connection conn) {
        System.out.println("List of MSAMDs:");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT msamd_name, msamd FROM preliminary;")) {
            while (rs.next()) {
                String msamdName = rs.getString("msamd_name");
                Integer msamd = rs.getInt("msamd");
    
                // Check for null MSAMD and skip if null
                if (rs.wasNull()) {
                    continue;
                }
    
                // Handle null or empty msamd_name
                if ( msamdName.trim().isEmpty()) {
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
        String query = "SELECT a.loan_amount_000s, a.rate_spread, a.lien_status " +
        "FROM applications a " +
        "JOIN preliminary p ON a.msamd = p.msamd " + // Adjust the JOIN condition based on your schema
        "WHERE a.action_taken = 1 AND a.purchaser_type IN (0, 1, 2, 3, 4, 8)";
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

        String updateQuery = "UPDATE applications SET purchaser_type = 9 WHERE action_taken = 1 AND purchaser_type IN (0, 1, 2, 3, 4, 8)";
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

    private static String getReadableFilters() {
        if (filters.isEmpty()) {
            return "No active filters.";
        }

        StringBuilder readableFilters = new StringBuilder();
        for (int i = 0; i < filters.size(); i++) {
            if (i > 0) {
                readableFilters.append(" AND ");
            }
            readableFilters.append(filters.get(i));
        }
        return readableFilters.toString();
    }
    private static void displayMatchingRowsAndLoanAmount(Connection conn) {
        if (filters.isEmpty()) {
            System.out.println("Matching Rows: 0");
            System.out.println("Total Loan Amount: 0");
            return;
        }
    
        boolean hasPreliminaryFilter = filters.stream().anyMatch(filter -> filter.contains("tract_to_msamd_income"));
        String baseQuery;
    
        if (hasPreliminaryFilter) {
            // Query directly from preliminary table
            baseQuery = "SELECT COUNT(*) AS row_count, SUM(loan_amount_000s) AS total_loan_amount " +
                        "FROM preliminary WHERE " + String.join(" AND ", filters);
        } else {
            // Query from applications table
            baseQuery = "SELECT COUNT(*) AS row_count, SUM(loan_amount_000s) AS total_loan_amount " +
                        "FROM applications WHERE action_taken = 1 AND purchaser_type IN (0, 1, 2, 3, 4, 8)";
            baseQuery += " AND " + String.join(" AND ", filters);
        }
    
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(baseQuery)) {
    
            if (rs.next()) {
                int rowCount = rs.getInt("row_count");
                double totalLoanAmount = rs.getDouble("total_loan_amount");
    
                System.out.println("Matching Rows: " + rowCount);
                System.out.println("Total Loan Amount: $" + (totalLoanAmount * 1000));
            } else {
                System.out.println("Matching Rows: 0");
                System.out.println("Total Loan Amount: 0");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
}
