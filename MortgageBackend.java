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
                System.out.println("1. Add Filter");
                System.out.println("2. Delete Filter");
                System.out.println("3. Calculate Rate");
                System.out.println("4. Add New Mortgage (Extra Credit)");
                System.out.println("5. List Available Counties");
                System.out.println("6. Exit");

                int choice = getIntInput(scanner, "Enter your choice: ");

                switch (choice) {
                    case 1:
                        addFilter(scanner, conn);
                        break;
                    case 2:
                        deleteFilter(scanner);
                        break;
                    case 3:
                        calculateRate(conn);
                        break;
                    case 4:
                        addNewMortgage(conn, scanner);
                        break;
                    case 5:
                        listCounties(conn);
                        break;
                    case 6:
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

    private static void addFilter(Scanner scanner, Connection conn) {
        System.out.println("Choose a filter type:");
        System.out.println("1. County");
        System.out.println("2. Loan Type");
        System.out.println("3. Income-to-Debt Ratio");
        int filterType = getIntInput(scanner, "Enter your choice: ");

        switch (filterType) {
            case 1:
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
                double minRatio = getDoubleInput(scanner, "Enter minimum income-to-debt ratio: ");
                double maxRatio = getDoubleInput(scanner, "Enter maximum income-to-debt ratio: ");
                filters.add("applicant_income_000s / loan_amount_000s BETWEEN " + minRatio + " AND " + maxRatio);
                System.out.println("Filter added: Income-to-Debt Ratio = " + minRatio + " to " + maxRatio);
                break;
            default:
                System.out.println("Invalid filter type.");
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

        String baseQuery = "SELECT loan_amount_000s, rate_spread, lien_status FROM applications WHERE action_taken = 1";
        baseQuery += " AND " + String.join(" AND ", filters);

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(baseQuery)) {

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

    private static void addNewMortgage(Connection conn, Scanner scanner) {
        try {
            int income = getIntInput(scanner, "Enter applicant income (000s): ");
            int loanAmount = getIntInput(scanner, "Enter loan amount (000s): ");
            int msamd = getIntInput(scanner, "Enter MSAMD: ");
            int sex = getIntInput(scanner, "Enter applicant sex (1=Male, 2=Female): ");
            int loanType = getIntInput(scanner, "Enter loan type (1-4): ");
            int ethnicity = getIntInput(scanner, "Enter applicant ethnicity (1-5): ");

            String query = "INSERT INTO applications (applicant_income_000s, loan_amount_000s, msamd, applicant_sex, loan_type, applicant_ethnicity) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, income);
            pstmt.setInt(2, loanAmount);
            pstmt.setInt(3, msamd);
            pstmt.setInt(4, sex);
            pstmt.setInt(5, loanType);
            pstmt.setInt(6, ethnicity);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("New mortgage added successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void listCounties(Connection conn) {
        System.out.println("Available Counties:");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT county_name, county_code FROM county_mapping")) {
            while (rs.next()) {
                System.out.println(rs.getString("county_name") + " (Code: " + rs.getInt("county_code") + ")");
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
