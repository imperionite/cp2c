package com.imperionite.cp2c.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.imperionite.cp2c.config.BigDecimalDeserializer; // Import the custom deserializer
import com.imperionite.cp2c.model.*; // Import all models from your package

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files; // NEW
import java.nio.file.Paths; // NEW
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for calculating employee monthly salary and government deductions.
 * Reads data from CSV and JSON files located in src/main/resources.
 */
public class SalaryCalculatorService {

    private static final String ATTENDANCE_FILE_RESOURCE = "/attendance.csv"; // Renamed
    private static final String CONTRIBUTIONS_FILE_RESOURCE = "/contributions.json"; // Renamed
    private final String employeeCsvFilePath; // NEW: Path for dynamic employee.csv

    private static final BigDecimal STANDARD_WORK_HOURS_PER_DAY = BigDecimal.valueOf(8);
    private static final BigDecimal STANDARD_WORK_DAYS_PER_MONTH = BigDecimal.valueOf(22);
    private static final BigDecimal STANDARD_MONTHLY_HOURS = STANDARD_WORK_HOURS_PER_DAY.multiply(STANDARD_WORK_DAYS_PER_MONTH);


    private final ObjectMapper objectMapper;
    // These lists are populated from CSVs/JSON for calculation purposes.
    // In a real app, employee data might come from a DB via EmployeeDao.
    // Here, SalaryCalculatorService independently loads its necessary data.
    private List<Employee> employeesForCalculation; // Renamed to avoid confusion with DAO-managed employees
    private List<AttendanceRecord> attendanceRecords;
    private ContributionConfig contributionConfig;

    // NEW: Constructor now accepts the employee CSV file path
    public SalaryCalculatorService(String employeeCsvFilePath) {
        this.employeeCsvFilePath = employeeCsvFilePath;
        this.objectMapper = new ObjectMapper();
        // Register modules for Java 8 Date/Time and custom BigDecimal deserialization
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.registerModule(new SimpleModule().addDeserializer(BigDecimal.class, new BigDecimalDeserializer()));

        loadData();
    }

    /**
     * Loads all necessary data (employees, attendance, contributions) from resource files.
     * This is called once during service initialization.
     */
    private void loadData() {
        try {
            // Load Employees from the file system path
            employeesForCalculation = loadEmployeesFromFileSystem(employeeCsvFilePath, this::parseEmployee); // NEW method call
            // Load Attendance from classpath resource
            attendanceRecords = loadResourceCsv(ATTENDANCE_FILE_RESOURCE, this::parseAttendanceRecord); // Renamed method
            // Load Contributions from classpath resource
            try (InputStream is = getClass().getResourceAsStream(CONTRIBUTIONS_FILE_RESOURCE)) { // Renamed constant
                if (is == null) {
                    throw new RuntimeException("Contributions file not found: " + CONTRIBUTIONS_FILE_RESOURCE);
                }
                this.contributionConfig = objectMapper.readValue(is, ContributionConfig.class);
                // Sort rules for correct lookup in calculation methods
                if (this.contributionConfig.getSss() != null) {
                    this.contributionConfig.getSss().sort(Comparator.comparing(SSSContributionRule::getSalaryCap));
                }
                if (this.contributionConfig.getPhilhealth() != null) {
                    this.contributionConfig.getPhilhealth().sort(Comparator.comparing(PhilHealthContributionRule::getMinSalary));
                }
                if (this.contributionConfig.getPagibig() != null) {
                    this.contributionConfig.getPagibig().sort(Comparator.comparing(PagIbigContributionRule::getSalaryCap));
                }
                if (this.contributionConfig.getWithholdingTax() != null) {
                    this.contributionConfig.getWithholdingTax().sort(Comparator.comparing(WithholdingTaxRule::getMinTaxableIncome));
                }

            }
            System.out.println("SalaryCalculatorService: Data loaded successfully.");
            System.out.println("SalaryCalculatorService: Loaded " + employeesForCalculation.size() + " employees, " + attendanceRecords.size() + " attendance records.");
        } catch (Exception e) {
            System.err.println("SalaryCalculatorService: Failed to load initial data: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to load initial data for salary calculations", e);
        }
    }

    /**
     * Helper method to load CSV files from classpath resources.
     * @param resourcePath Path to the CSV file in resources.
     * @param parser A functional interface to parse each line into a specific object type.
     * @param <T> The type of object to parse each line into.
     * @return A list of parsed objects.
     * @throws Exception if file cannot be read or parsed.
     */
    private <T> List<T> loadResourceCsv(String resourcePath, CsvLineParser<T> parser) throws Exception { // Renamed
        List<T> records = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream(resourcePath);) {
             if (is == null) {
                 throw new RuntimeException("Resource CSV file not found in classpath: " + resourcePath); // More specific error message
             }
             BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false; // Skip header line
                    continue;
                }
                if (line.trim().isEmpty()) continue; // Skip empty lines
                records.add(parser.parse(line));
            }
        }
        return records;
    }

    /**
     * NEW: Helper method to load CSV files from the file system.
     * @param filePath Full path to the CSV file on the file system.
     * @param parser A functional interface to parse each line into a specific object type.
     * @param <T> The type of object to parse each line into.
     * @return A list of parsed objects.
     * @throws Exception if file cannot be read or parsed.
     */
    private <T> List<T> loadEmployeesFromFileSystem(String filePath, CsvLineParser<T> parser) throws Exception {
        List<T> records = new ArrayList<>();
        // Use try-with-resources for BufferedReader
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false; // Skip header line
                    continue;
                }
                if (line.trim().isEmpty()) continue; // Skip empty lines
                records.add(parser.parse(line));
            }
        } catch (java.nio.file.NoSuchFileException e) {
            throw new RuntimeException("File system CSV not found: " + filePath + ". Please ensure it exists and is accessible.", e);
        }
        return records;
    }


    /** Functional interface for parsing a single CSV line. */
    @FunctionalInterface
    private interface CsvLineParser<T> {
        T parse(String line);
    }

    /**
     * Parses a single line from the employee.csv file into an Employee object.
     * Adapts to Employee.java's String birthday format.
     * @param line The CSV line.
     * @return An Employee object.
     * @throws RuntimeException if parsing fails.
     */
    private Employee parseEmployee(String line) {
        String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Split by comma, ignore commas inside quotes
        Employee employee = new Employee();
        try {
            // Note: Using your Employee.java's String type for birthday
            employee.setEmployeeNumber(parts[0].trim());
            employee.setLastName(parts[1].trim());
            employee.setFirstName(parts[2].trim());
            employee.setBirthday(parts[3].trim()); // Storing as String to match your Employee model
            employee.setAddress(parts[4].trim().replace("\"", "")); // Remove outer quotes from address
            employee.setPhoneNumber(parts[5].trim());
            employee.setSssNumber(parts[6].trim());
            employee.setPhilhealthNumber(parts[7].trim());
            employee.setTinNumber(parts[8].trim());
            employee.setPagibigNumber(parts[9].trim());
            employee.setStatus(parts[10].trim());
            employee.setPosition(parts[11].trim());
            employee.setImmediateSupervisor(parts[12].trim());

            // FIX: Remove both commas AND quotation marks from numeric strings before converting to BigDecimal
            employee.setBasicSalary(new BigDecimal(parts[13].trim().replace(",", "").replace("\"", "")));
            employee.setRiceSubsidy(new BigDecimal(parts[14].trim().replace(",", "").replace("\"", "")));
            employee.setPhoneAllowance(new BigDecimal(parts[15].trim().replace(",", "").replace("\"", "")));
            employee.setClothingAllowance(new BigDecimal(parts[16].trim().replace(",", "").replace("\"", "")));
            employee.setGrossSemiMonthlyRate(new BigDecimal(parts[17].trim().replace(",", "").replace("\"", "")));
            employee.setHourlyRate(new BigDecimal(parts[18].trim().replace(",", "").replace("\"", "")));
        } catch (Exception e) {
            System.err.println("SalaryCalculatorService: Error parsing employee line: " + line + " -> " + e.getMessage());
            throw new RuntimeException("Failed to parse employee line: " + line, e);
        }
        return employee;
    }

    /**
     * Parses a single line from the attendance.csv file into an AttendanceRecord object.
     * @param line The CSV line.
     * @return An AttendanceRecord object.
     * @throws RuntimeException if parsing fails.
     */
    private AttendanceRecord parseAttendanceRecord(String line) {
        String[] parts = line.split(",");
        AttendanceRecord record = new AttendanceRecord();
        try {
            System.out.println("DEBUG ATTENDANCE: Line: " + line);
            for (int i = 0; i < parts.length; i++) {
                System.out.println("DEBUG ATTENDANCE: parts[" + i + "]: '" + parts[i].trim() + "'");
            }

            // Adjusted indices based on provided attendance.csv header: EmployeeNumber,LastName,FirstName,Date,LogIn,LogOut
            record.setEmployeeNumber(parts[0].trim());
            // Explicitly define date and time formatters
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            record.setDate(LocalDate.parse(parts[3].trim(), dateFormatter)); // Date is at index 3
            record.setLoginTime(LocalTime.parse(parts[4].trim(), timeFormatter)); // Login time is at index 4
            record.setLogoutTime(LocalTime.parse(parts[5].trim(), timeFormatter)); // Logout time is at index 5
        } catch (Exception e) {
            System.err.println("SalaryCalculatorService: Error parsing attendance line: " + line + " -> " + e.getMessage());
            throw new RuntimeException("Failed to parse attendance line: " + line, e);
        }
        return record;
    }

    /**
     * Retrieves a sorted list of unique YearMonth periods found in the attendance records.
     * These represent the available pay periods for salary calculation.
     * @return A list of MonthlyCutoff objects.
     */
    public List<MonthlyCutoff> getMonthlyCutoffs() {
        Set<YearMonth> uniqueYearMonths = new TreeSet<>(); // Use TreeSet to keep them sorted
        DateTimeFormatter monthDayFormatter = DateTimeFormatter.ofPattern("MMM d");

        for (AttendanceRecord record : attendanceRecords) {
            uniqueYearMonths.add(YearMonth.from(record.getDate()));
        }

        List<MonthlyCutoff> cutoffs = new ArrayList<>();
        for (YearMonth ym : uniqueYearMonths) {
            LocalDate firstDayOfMonth = ym.atDay(1);
            LocalDate lastDayOfMonth = ym.atEndOfMonth();
            String yearMonthStr = ym.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String startDate = firstDayOfMonth.format(monthDayFormatter);
            String endDate = lastDayOfMonth.format(monthDayFormatter);
            cutoffs.add(new MonthlyCutoff(yearMonthStr, startDate, endDate));
        }
        return cutoffs;
    }

    /**
     * Calculates the full monthly salary (gross, deductions, net) for a given employee and month.
     * @param employeeNumber The unique identifier of the employee.
     * @param yearMonth The month for which to calculate salary (e.g., "2024-01").
     * @return A MonthlySalaryCalculationResult object.
     * @throws IllegalArgumentException if the employee is not found or no attendance data exists for the period.
     */
    public MonthlySalaryCalculationResult calculateMonthlySalary(String employeeNumber, String yearMonth) {
        Employee employee = employeesForCalculation.stream() // Use employeesForCalculation
                .filter(e -> e.getEmployeeNumber().equals(employeeNumber))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeNumber));

        YearMonth targetYearMonth = YearMonth.parse(yearMonth);

        List<AttendanceRecord> employeeMonthlyAttendance = attendanceRecords.stream()
                .filter(record -> record.getEmployeeNumber().equals(employeeNumber) &&
                                 YearMonth.from(record.getDate()).equals(targetYearMonth))
                .collect(Collectors.toList());

        // FIX: Throw an exception if no attendance records are found for the specific month.
        if (employeeMonthlyAttendance.isEmpty()) {
            throw new IllegalArgumentException("No attendance records found for employee " + employeeNumber + " for month " + yearMonth + ". Cannot calculate salary.");
        }

        double totalActualWorkedHours = employeeMonthlyAttendance.stream()
            .mapToDouble(record -> {
                if (record.getLoginTime() == null || record.getLogoutTime() == null || record.getLoginTime().isAfter(record.getLogoutTime())) {
                    return 0;
                }
                Duration duration = Duration.between(record.getLoginTime(), record.getLogoutTime());
                // Cap daily hours at STANDARD_WORK_HOURS_PER_DAY (8.0) for calculation base
                return Math.min(duration.toMinutes() / 60.0, STANDARD_WORK_HOURS_PER_DAY.doubleValue());
            })
            .sum();

        // Calculate Gross Monthly Salary based on prorated basic salary + allowances
        BigDecimal proratedBasicSalary;
        if (totalActualWorkedHours >= STANDARD_MONTHLY_HOURS.doubleValue()) {
             proratedBasicSalary = employee.getBasicSalary();
        } else {
             proratedBasicSalary = employee.getBasicSalary()
                                        .multiply(BigDecimal.valueOf(totalActualWorkedHours))
                                        .divide(STANDARD_MONTHLY_HOURS, 2, RoundingMode.HALF_UP);
        }

        BigDecimal grossMonthlySalary = proratedBasicSalary
                .add(employee.getRiceSubsidy())
                .add(employee.getPhoneAllowance())
                .add(employee.getClothingAllowance())
                .setScale(2, RoundingMode.HALF_UP);

        // --- Calculate Mandated Deductions ---
        BigDecimal monthlySssDeduction = calculateSssDeduction(grossMonthlySalary);
        BigDecimal monthlyPhilhealthDeduction = calculatePhilhealthDeduction(grossMonthlySalary);
        BigDecimal monthlyPagibigDeduction = calculatePagibigDeduction(grossMonthlySalary);

        // Calculate Taxable Income: Gross Salary - Mandated Deductions (SSS, PhilHealth, Pag-IBIG employee shares)
        BigDecimal taxableIncome = grossMonthlySalary
            .subtract(monthlySssDeduction)
            .subtract(monthlyPhilhealthDeduction)
            .subtract(monthlyPagibigDeduction)
            .setScale(2, RoundingMode.HALF_UP);

        // Ensure taxable income is not negative
        if (taxableIncome.compareTo(BigDecimal.ZERO) < 0) {
            taxableIncome = BigDecimal.ZERO;
        }

        BigDecimal monthlyWithholdingTax = calculateWithholdingTax(taxableIncome);

        BigDecimal totalDeductions = monthlySssDeduction
                .add(monthlyPhilhealthDeduction)
                .add(monthlyPagibigDeduction)
                .add(monthlyWithholdingTax)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal netMonthlySalary = grossMonthlySalary.subtract(totalDeductions)
                .setScale(2, RoundingMode.HALF_UP);

        return new MonthlySalaryCalculationResult(
                employeeNumber,
                yearMonth,
                totalActualWorkedHours,
                grossMonthlySalary,
                monthlySssDeduction,
                monthlyPhilhealthDeduction,
                monthlyPagibigDeduction,
                monthlyWithholdingTax,
                totalDeductions,
                netMonthlySalary
        );
    }

    /**
     * Calculates the SSS employee contribution based on gross monthly salary and rules.
     * Rules are assumed to be sorted by salaryCap in ascending order.
     * @param grossMonthlySalary The employee's gross monthly salary.
     * @return The calculated SSS deduction.
     */
    private BigDecimal calculateSssDeduction(BigDecimal grossMonthlySalary) {
        if (contributionConfig == null || contributionConfig.getSss() == null || contributionConfig.getSss().isEmpty()) {
            System.err.println("SalaryCalculatorService: SSS contribution rules not loaded.");
            return BigDecimal.ZERO;
        }

        // Rules are pre-sorted during loadData
        for (SSSContributionRule rule : contributionConfig.getSss()) {
            if (grossMonthlySalary.compareTo(rule.getSalaryCap()) <= 0) {
                return rule.getContribution().setScale(2, RoundingMode.HALF_UP);
            }
        }
        // Fallback for amounts exceeding max cap (should be the last rule due to sorting)
        return contributionConfig.getSss().get(contributionConfig.getSss().size() - 1).getContribution().setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the PhilHealth employee contribution based on gross monthly salary and rules.
     * Rules are assumed to be sorted by minSalary in ascending order.
     * @param grossMonthlySalary The employee's gross monthly salary.
     * @return The calculated PhilHealth deduction.
     */
    private BigDecimal calculatePhilhealthDeduction(BigDecimal grossMonthlySalary) {
        if (contributionConfig == null || contributionConfig.getPhilhealth() == null || contributionConfig.getPhilhealth().isEmpty()) {
            System.err.println("SalaryCalculatorService: PhilHealth contribution rules not loaded.");
            return BigDecimal.ZERO;
        }

        // Rules are pre-sorted during loadData
        for (PhilHealthContributionRule rule : contributionConfig.getPhilhealth()) {
            // Check if grossMonthlySalary falls within the current rule's salary range
            if (grossMonthlySalary.compareTo(rule.getMinSalary()) >= 0 && grossMonthlySalary.compareTo(rule.getMaxSalary()) <= 0) {
                if (rule.getFixedEmployeeContribution() != null) {
                    return rule.getFixedEmployeeContribution().setScale(2, RoundingMode.HALF_UP);
                } else {
                    // For percentage-based rates, employee share is typically half the total rate
                    BigDecimal employeeShareRate = rule.getRate().divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
                    BigDecimal contribution = grossMonthlySalary.multiply(employeeShareRate);
                    return contribution.setScale(2, RoundingMode.HALF_UP);
                }
            }
        }
        // Fallback if no rule matches (should not happen if ranges cover all possibilities)
        System.err.println("SalaryCalculatorService: No PhilHealth rule matched for salary: " + grossMonthlySalary);
        return BigDecimal.ZERO;
    }

    /**
     * Calculates the Pag-IBIG employee contribution based on gross monthly salary and rules.
     * Rules are assumed to be sorted by salaryCap in ascending order.
     * @param grossMonthlySalary The employee's gross monthly salary.
     * @return The calculated Pag-IBIG deduction.
     */
    private BigDecimal calculatePagibigDeduction(BigDecimal grossMonthlySalary) {
        if (contributionConfig == null || contributionConfig.getPagibig() == null || contributionConfig.getPagibig().isEmpty()) {
            System.err.println("SalaryCalculatorService: Pag-IBIG contribution rules not loaded.");
            return BigDecimal.ZERO;
        }

        BigDecimal pagibigFundSalaryCreditCap = BigDecimal.valueOf(5000); // Max salary credit for Pag-IBIG purposes
        BigDecimal applicableSalary = grossMonthlySalary.min(pagibigFundSalaryCreditCap);

        // Rules are pre-sorted during loadData
        BigDecimal rate = BigDecimal.ZERO;
        for (PagIbigContributionRule rule : contributionConfig.getPagibig()) {
            if (grossMonthlySalary.compareTo(rule.getSalaryCap()) <= 0) {
                rate = rule.getContributionRate();
                break;
            }
        }

        // If grossMonthlySalary exceeds all caps, use the rate of the last rule.
        if (rate.compareTo(BigDecimal.ZERO) == 0 && !contributionConfig.getPagibig().isEmpty()) {
            rate = contributionConfig.getPagibig().get(contributionConfig.getPagibig().size() - 1).getContributionRate();
        }

        BigDecimal calculatedContribution = applicableSalary.multiply(rate);
        // Pag-IBIG employee share has a maximum of P100 (for 2% contribution based on 5k credit).
        return calculatedContribution.min(BigDecimal.valueOf(100.00)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the monthly withholding tax based on taxable income and rules.
     * Taxable income = Gross Salary - (SSS + PhilHealth + Pag-IBIG employee shares).
     * Rules are assumed to be sorted by minTaxableIncome in ascending order.
     * @param taxableIncome The employee's taxable income for the month.
     * @return The calculated withholding tax.
     */
    private BigDecimal calculateWithholdingTax(BigDecimal taxableIncome) {
        if (contributionConfig == null || contributionConfig.getWithholdingTax() == null || contributionConfig.getWithholdingTax().isEmpty()) {
            System.err.println("SalaryCalculatorService: Withholding tax rules not loaded.");
            return BigDecimal.ZERO;
        }

        // Rules are pre-sorted during loadData
        for (WithholdingTaxRule rule : contributionConfig.getWithholdingTax()) {
            // Check if taxableIncome falls within the current rule's range
            if (taxableIncome.compareTo(rule.getMinTaxableIncome()) >= 0 && taxableIncome.compareTo(rule.getMaxTaxableIncome()) <= 0) {
                BigDecimal tax = rule.getFixedTax();
                if (rule.getPercentageOver().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal taxableExcess = taxableIncome.subtract(rule.getExcessOver());
                    tax = tax.add(taxableExcess.multiply(rule.getPercentageOver()));
                }
                return tax.setScale(2, RoundingMode.HALF_UP);
            }
        }
        // Fallback: If no rule matches (e.g., taxable income exceeds highest bracket),
        // This indicates an issue with the tax table ranges as the last rule should cover everything.
        System.err.println("SalaryCalculatorService: No withholding tax rule matched for taxable income: " + taxableIncome);
        return BigDecimal.ZERO;
    }
}
