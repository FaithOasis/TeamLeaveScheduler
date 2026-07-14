package com.faithoasiscodes.LeaveRequest;


import com.faithoasiscodes.Employee.Employee;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leaves")
@CrossOrigin(origins = "*")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @GetMapping("/employees")
    public ResponseEntity<List<Employee>> getEmployees() {
        return ResponseEntity.ok(leaveRequestService.getAllEmployees());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<LeaveRequest>> getPendingRequests() {
        return ResponseEntity.ok(leaveRequestService.getPendingRequests());
    }

    @GetMapping("/approved")
    public ResponseEntity<List<LeaveRequest>> getApprovedRequests() {
        return ResponseEntity.ok(leaveRequestService.getApprovedRequests());
    }

    @GetMapping("/calendar")
    public ResponseEntity<Map<LocalDate, List<String>>> getCalendar() {
        return ResponseEntity.ok(leaveRequestService.get30DayCalendarGrid());
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitRequest(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam LeaveType type) {
        try {
            LeaveRequest result = leaveRequestService.submitLeaveRequest(employeeId, startDate, endDate, type);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/evaluate")
    public ResponseEntity<?> evaluateRequest(
            @RequestParam Long requestId,
            @RequestParam LeaveStatus decision) {
        try {
            leaveRequestService.evaluateLeaveRequest(requestId, decision);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}