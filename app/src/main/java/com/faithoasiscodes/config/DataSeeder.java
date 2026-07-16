package com.faithoasiscodes.config;
import com.faithoasiscodes.Employee.Employee;
import com.faithoasiscodes.Employee.Team;
import com.faithoasiscodes.PublicHoliday.PublicHoliday;
import com.faithoasiscodes.PublicHoliday.PublicHolidayRepository;
import com.faithoasiscodes.Employee.EmployeeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
@Component
public class DataSeeder implements CommandLineRunner {
    private final EmployeeRepository employeeRepository;
    private final PublicHolidayRepository publicHolidayRepository;

    public DataSeeder(EmployeeRepository employeeRepository,
                      PublicHolidayRepository publicHolidayRepository) {
        this.employeeRepository = employeeRepository;
        this.publicHolidayRepository = publicHolidayRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        seedEmployees();
        seedHolidays();
    }

    private void seedEmployees() throws IOException {
        if (employeeRepository.count() > 0) {
            return; // already seeded — avoids duplicating rows on every restart
        }
        ClassPathResource resource = new ClassPathResource("employees.csv");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // skip header row
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split(",");
                Employee employee = new Employee();
                employee.setName(parts[1].trim());
                employee.setTeam(Team.valueOf(parts[2].trim()));
                employeeRepository.save(employee);
            }
        }
    }

    private void seedHolidays() throws IOException {
        if (publicHolidayRepository.count() > 0) {
            return;
        }
        ClassPathResource resource = new ClassPathResource("public_holidays.json");
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> raw = mapper.readValue(
                resource.getInputStream(), new TypeReference<List<Map<String, String>>>() {});
        for (Map<String, String> entry : raw) {
            LocalDate date = LocalDate.parse(entry.get("date"));
            publicHolidayRepository.save(new PublicHoliday(date, entry.get("name")));
        }
    }
}
