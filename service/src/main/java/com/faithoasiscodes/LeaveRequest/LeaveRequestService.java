package com.faithoasiscodes.LeaveRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.faithoasiscodes.Employee.Employee;
import com.faithoasiscodes.Employee.EmployeeRepository;
import com.faithoasiscodes.Employee.Team;
import com.faithoasiscodes.PublicHoliday.PublicHolidayRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final PublicHolidayRepository publicHolidayRepository;

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public List<LeaveRequest> getPendingRequests() {
        return leaveRequestRepository.findByStatus(LeaveStatus.PENDING);
    }

    public List<LeaveRequest> getApprovedRequests() {
        return leaveRequestRepository.findByStatus(LeaveStatus.APPROVED);
    }

    @Transactional
    public LeaveRequest submitLeaveRequest(Long employeeId, LocalDate startDate, LocalDate endDate, LeaveType type) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        // Overlap Exception check: Match against employee's existing APPROVED requests
        List<LeaveRequest> overlaps = leaveRequestRepository
                .findByEmployeeAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        employee, LeaveStatus.APPROVED, endDate, startDate
                );
        if (!overlaps.isEmpty()) {
            throw new IllegalStateException("Request overlap exception: Employee already has an approved leave during this range.");
        }

        LeaveRequest request = new LeaveRequest();
        request.setEmployee(employee);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setStatus(LeaveStatus.PENDING);
        request.setType(type);
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        return leaveRequestRepository.save(request);
    }

    @Transactional
    public void evaluateLeaveRequest(Long requestId, LeaveStatus decision) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found with ID: " + requestId));

        if (decision == LeaveStatus.REJECTED) {
            request.setStatus(LeaveStatus.REJECTED);
            request.setUpdatedAt(LocalDateTime.now());
            leaveRequestRepository.save(request);
            return;
        }

        Employee employee = request.getEmployee();
        final Team team = employee.getTeam(); // Marked final for lambda safety

        // 30% boundary calculation with rounding down
        long totalTeamMembers = employeeRepository.countByTeam(team);
        int maxAllowedOnLeave = (int) Math.floor(totalTeamMembers * 0.30);

        // Fetch all approved requests overlapping this request's window
        List<LeaveRequest> activeApprovedLeaves = leaveRequestRepository
                .findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        LeaveStatus.APPROVED, request.getEndDate(), request.getStartDate()
                );

        int workingDaysToDeduct = 0;

        // Day-by-day validation
        for (LocalDate date = request.getStartDate(); !date.isAfter(request.getEndDate()); date = date.plusDays(1)) {
            // Check weekend and public holiday exceptions
            if (isWeekend(date) || isPublicHoliday(date)) {
                continue; // Skip assessment on non-working days
            }

            workingDaysToDeduct++;

            // Create a final copy of the current loop date to use inside the lambda expression safely
            final LocalDate currentLoopDate = date;

            // Count peers on leave for this specific day
            long activeCount = activeApprovedLeaves.stream()
                    .filter(lr -> lr.getEmployee().getTeam() == team)
                    .filter(lr -> !lr.getStartDate().isAfter(currentLoopDate) && !lr.getEndDate().isBefore(currentLoopDate))
                    .count();

            if (activeCount + 1 > maxAllowedOnLeave) {
                throw new IllegalStateException("30% Boundary Rule Exception: Allowing this request violates team limits on " + date + ".");
            }
        }

        if (employee.getLeaveBalance() < workingDaysToDeduct) {
            throw new IllegalArgumentException("Insufficient leave balance. Required: " + workingDaysToDeduct + ", Available: " + employee.getLeaveBalance());
        }

        // Apply deductions & update state
        employee.setLeaveBalance(employee.getLeaveBalance() - workingDaysToDeduct);
        employeeRepository.save(employee);

        request.setStatus(LeaveStatus.APPROVED);
        request.setUpdatedAt(LocalDateTime.now());
        leaveRequestRepository.save(request);
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private boolean isPublicHoliday(LocalDate date) {
        return publicHolidayRepository.existsByDate(date);
    }

    // Helper payload generation for your 30-day dashboard grid view
    public Map<LocalDate, List<String>> get30DayCalendarGrid() {
        Map<LocalDate, List<String>> calendarMap = new TreeMap<>();
        LocalDate startPoint = LocalDate.now();

        for (int i = 0; i < 30; i++) {
            calendarMap.put(startPoint.plusDays(i), new ArrayList<>());
        }

        List<LeaveRequest> approvedList = leaveRequestRepository.findByStatus(LeaveStatus.APPROVED);
        for (LeaveRequest lr : approvedList) {
            for (LocalDate date = lr.getStartDate(); !date.isAfter(lr.getEndDate()); date = date.plusDays(1)) {
                if (calendarMap.containsKey(date)) {
                    calendarMap.get(date).add(lr.getEmployee().getName() + " (" + lr.getEmployee().getTeam() + ")");
                }
            }
        }
        return calendarMap;
    }
}