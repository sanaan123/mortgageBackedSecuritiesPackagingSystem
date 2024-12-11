import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

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
                System.out.println("5. Add New Mortgage");
            
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
                    case 5:
                        addNewMortgage(conn);
                        break;
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
                listLoanTypes(conn); // Display loan type options
                System.out.print("Enter loan types (comma-separated, e.g., 1,2 for Conventional and FHA-insured): ");
                String loanTypeInput = scanner.nextLine().trim();
                String[] loanTypeArray = loanTypeInput.split(",");
            
                // Build the filter for multiple loan types
                StringBuilder loanTypeFilter = new StringBuilder("loan_type IN (");
                for (int i = 0; i < loanTypeArray.length; i++) {
                    loanTypeFilter.append(loanTypeArray[i].trim());
                    if (i < loanTypeArray.length - 1) {
                        loanTypeFilter.append(", ");
                    }
                }
                loanTypeFilter.append(")");
            
                // Add the filter to the list
                filters.add(loanTypeFilter.toString());
                System.out.println("Filter added: " + loanTypeFilter.toString());
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
                System.out.println("List of Loan Purposes:");
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT DISTINCT loan_purpose_name, loan_purpose FROM preliminary ORDER BY loan_purpose ASC;")) {
                    while (rs.next()) {
                        String loanPurposeName = rs.getString("loan_purpose_name");
                        int loanPurpose = rs.getInt("loan_purpose");
                        System.out.println(loanPurpose + " - " + loanPurposeName);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            
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
                System.out.println("Property Types:");
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT DISTINCT property_type_name, property_type FROM preliminary ORDER BY property_type")) {
                    while (rs.next()) {
                        int propertyTypeCode = rs.getInt("property_type");
                        String propertyTypeName = rs.getString("property_type_name");
                        System.out.println(propertyTypeCode + " - " + propertyTypeName);
                    }
                } catch (SQLException e) {
                    System.out.println("Error fetching property types: " + e.getMessage());
                    e.printStackTrace();
                    return; // Exit if there's an error fetching property types
                }
            
                System.out.print("Enter property type codes (comma-separated): ");
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
            
                // Add the filter
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
    //since our database was not formatted correctly i worked around it by querying preliminary and applications separately, then assigning the rows into a hashmap and then comparing the two tables
    //tried to do a join with the applications and preliminary table but it did not work/took too long
    try {
        // Begin transaction
        conn.setAutoCommit(false);
    
        // Separate filters for each table
        List<String> applicationsFilters = new ArrayList<>();
        List<String> preliminaryFilters = new ArrayList<>();
    
        for (String filter : filters) {
            if (filter.contains("tract_to_msamd_income")) {
                preliminaryFilters.add(filter); // Only for preliminary table
            } else {
                applicationsFilters.add(filter); // Only for applications table
            }
        }
    
        // Query applications table
        String applicationsQuery = "SELECT loan_amount_000s, rate_spread, lien_status "
                + "FROM applications "
                + (applicationsFilters.isEmpty() ? "" : "WHERE action_taken = 1 AND purchaser_type IN (0, 1, 2, 3, 4, 8) AND " + String.join(" AND ", applicationsFilters));
    
        List<Map<String, Object>> applicationsResults = new ArrayList<>();
        if (!applicationsFilters.isEmpty()) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(applicationsQuery)) {
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("loan_amount", rs.getDouble("loan_amount_000s"));
                    record.put("rate_spread", rs.getDouble("rate_spread"));
                    record.put("lien_status", rs.getInt("lien_status"));
                    applicationsResults.add(record);
                }
            }
        }
    
        // Query preliminary table
        String preliminaryQuery = "SELECT loan_amount_000s, rate_spread, lien_status "
                + "FROM preliminary "
                + (preliminaryFilters.isEmpty() ? "" : "WHERE " + String.join(" AND ", preliminaryFilters));
    
        List<Map<String, Object>> preliminaryResults = new ArrayList<>();
        if (!preliminaryFilters.isEmpty()) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(preliminaryQuery)) {
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("loan_amount", rs.getDouble("loan_amount_000s"));
                    record.put("rate_spread", rs.getDouble("rate_spread"));
                    record.put("lien_status", rs.getInt("lien_status"));
                    preliminaryResults.add(record);
                }
            }
        }
    
        // Combine results
        Set<String> uniqueRecords = new HashSet<>();
        double totalLoanAmount = 0;
        double weightedRateSum = 0;
    
        for (Map<String, Object> record : applicationsResults) {
            String uniqueKey = record.get("loan_amount").toString() + "_" + record.get("rate_spread");
            if (!uniqueRecords.contains(uniqueKey)) {
                uniqueRecords.add(uniqueKey);
    
                double loanAmount = (double) record.get("loan_amount");
                double rateSpread = (double) record.get("rate_spread");
                int lienStatus = (int) record.get("lien_status");
    
                if (rateSpread == 0.0) {
                    rateSpread = (lienStatus == 1) ? 1.5 : 3.5;
                }
    
                weightedRateSum += (rateSpread + 2.33) * loanAmount;
                totalLoanAmount += loanAmount;
            }
        }
    
        for (Map<String, Object> record : preliminaryResults) {
            String uniqueKey = record.get("loan_amount").toString() + "_" + record.get("rate_spread");
            if (!uniqueRecords.contains(uniqueKey)) {
                uniqueRecords.add(uniqueKey);
    
                double loanAmount = (double) record.get("loan_amount");
                double rateSpread = (double) record.get("rate_spread");
                int lienStatus = (int) record.get("lien_status");
    
                if (rateSpread == 0.0) {
                    rateSpread = (lienStatus == 1) ? 1.5 : 3.5;
                }
    
                weightedRateSum += (rateSpread + 2.33) * loanAmount;
                totalLoanAmount += loanAmount;
            }
        }
    
        // Calculate final rate
        if (totalLoanAmount > 0) {
            double weightedRate = weightedRateSum / totalLoanAmount;
            System.out.println("Weighted Rate: " + weightedRate);
            System.out.println("Total Loan Amount: $" + totalLoanAmount);
    
            // Ask user to accept or decline the rate
            Scanner scanner = new Scanner(System.in);
            System.out.print("Do you accept this rate and total cost? (yes/no): ");
            String response = scanner.nextLine().trim().toLowerCase();
    
            if (response.equals("yes")) {
                // Update purchaser_type to 9 (Private Securitization)
                String updateQuery = "UPDATE applications SET purchaser_type = 9 WHERE action_taken = 1 AND purchaser_type IN (0, 1, 2, 3, 4, 8) "
                        + (applicationsFilters.isEmpty() ? "" : "AND " + String.join(" AND ", applicationsFilters));
    
                try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                    int updatedRows = pstmt.executeUpdate();
                    if (updatedRows > 0) {
                        System.out.println(updatedRows + " mortgages successfully updated to Private Securitization.");
                        conn.commit(); // Commit the transaction
                        System.exit(0); // Exit the program
                    } else {
                        System.out.println("No mortgages matched the filters for update.");
                        conn.rollback(); // Rollback if no updates
                    }
                }
            } else {
                System.out.println("Rate and total cost declined. Returning to main menu...");
            }
        } else {
            System.out.println("No results match the filters.");
        }
    } catch (SQLException e) {
        try {
            conn.rollback(); // Rollback on error
        } catch (SQLException rollbackEx) {
            System.out.println("Rollback failed: " + rollbackEx.getMessage());
        }
        System.out.println("Transaction failed: " + e.getMessage());
    } finally {
        try {
            conn.setAutoCommit(true); // Reset auto-commit
        } catch (SQLException ex) {
            System.out.println("Failed to reset auto-commit: " + ex.getMessage());
        }
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
        for (String filter : filters) {
            if (filter.contains("loan_purpose")) {
                if (filter.contains("1")) readableFilters.append("Loan Purpose: Home improvement\n");
                if (filter.contains("2")) readableFilters.append("Loan Purpose: Home purchase\n");
                if (filter.contains("3")) readableFilters.append("Loan Purpose: Refinancing\n");
            } else if (filter.contains("loan_type")) {
                if (filter.contains("1")) readableFilters.append("Loan Type: Conventional\n");
                if (filter.contains("2")) readableFilters.append("Loan Type: FHA-insured\n");
                if (filter.contains("3")) readableFilters.append("Loan Type: FSA/RHS-guaranteed\n");
                if (filter.contains("4")) readableFilters.append("Loan Type: VA-guaranteed\n");
            } else if (filter.contains("property_type")) {
                if (filter.contains("1")) readableFilters.append("Property Type: One-to-four family dwelling\n");
                if (filter.contains("2")) readableFilters.append("Property Type: Manufactured housing\n");
                if (filter.contains("3")) readableFilters.append("Property Type: Multifamily dwelling\n");
            } else if (filter.contains("tract_to_msamd_income")) {
                readableFilters.append(filter.replace("tract_to_msamd_income", "Tract-to-MSAMD Income")).append("\n");
            } else if (filter.contains("msamd")) {
                readableFilters.append(filter.replace("msamd", "MSAMD")).append("\n");
            } else if (filter.contains("applicant_income_000s / loan_amount_000s")) {
                readableFilters.append(filter.replace("applicant_income_000s / loan_amount_000s", "Income-to-Debt Ratio")).append("\n");
            } else {
                readableFilters.append(filter).append("\n");
            }
        }
    
        return readableFilters.toString().trim();
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
    
    private static void listLoanTypes(Connection conn) {
        System.out.println("List of Loan Types:");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT loan_type, loan_type_name FROM preliminary;")) {
            while (rs.next()) {
                int loanType = rs.getInt("loan_type");
                String loanTypeName = rs.getString("loan_type_name");
                System.out.println(loanType + " - " + loanTypeName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void addNewMortgage(Connection conn) {
        try {
            Scanner scanner = new Scanner(System.in);
    
            System.out.print("Enter Applicant Income (in thousands): ");
            int income = getIntInput(scanner, "Income: ");
    
            System.out.print("Enter Loan Amount (in thousands): ");
            int loanAmount = getIntInput(scanner, "Loan Amount: ");
    
            System.out.print("Enter MSAMD: ");
            int msamd = getIntInput(scanner, "MSAMD: ");
    
            System.out.println("Select Applicant Sex:");
            System.out.println("1 - Male");
            System.out.println("2 - Female");
            System.out.println("3 - Unknown");
            int sex = getIntInput(scanner, "Sex: ");
    
            System.out.println("Select Loan Type:");
            listLoanTypes(conn); // Display loan type options
            int loanType = getIntInput(scanner, "Loan Type: ");
    
            System.out.println("Select Applicant Ethnicity:");
            System.out.println("1 - Hispanic or Latino");
            System.out.println("2 - Not Hispanic or Latino");
            System.out.println("3 - Unknown");
            int ethnicity = getIntInput(scanner, "Ethnicity: ");
    
            // Get the first matching location_id for the given MSAMD
            int locationId = 0;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT location_id FROM locations WHERE msamd = ? LIMIT 1")) {
                stmt.setInt(1, msamd);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        locationId = rs.getInt("location_id");
                    }
                }
            }
    
            if (locationId == 0) {
                System.out.println("No matching location found for MSAMD " + msamd);
                return;
            }
    
            // Insert new mortgage into applications table
            String insertQuery = "INSERT INTO applications (applicant_income_000s, loan_amount_000s, msamd, applicant_sex, loan_type, applicant_ethnicity, location_id) "
                               + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                pstmt.setInt(1, income);
                pstmt.setInt(2, loanAmount);
                pstmt.setInt(3, msamd);
                pstmt.setInt(4, sex);
                pstmt.setInt(5, loanType);
                pstmt.setInt(6, ethnicity);
                pstmt.setInt(7, locationId);
    
                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("New mortgage successfully added.");
                } else {
                    System.out.println("Failed to add new mortgage.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error adding new mortgage: " + e.getMessage());
        }
    }
    
    
}
