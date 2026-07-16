package com.faithoasiscodes.LeaveRequest;

import com.faithoasiscodes.Employee.Employee;
import com.faithoasiscodes.Employee.EmployeeRepository;
import com.faithoasiscodes.Employee.Team;
import com.faithoasiscodes.PublicHoliday.PublicHolidayRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private PublicHolidayRepository publicHolidayRepository;

    @InjectMocks
    private LeaveRequestService leaveRequestService;

    private Employee alice;
    private Employee bob;

    @BeforeEach
    void setUp() {
        alice = new Employee();
        alice.setId(1L);
        alice.setName("Alice");
        alice.setTeam(Team.ENGINEERING);

        bob = new Employee();
        bob.setId(2L);
        bob.setName("Bob");
        bob.setTeam(Team.ENGINEERING);
    }

    // ============================================================
    // 1. OVERLAP RULE TEST (required)
    // ============================================================
    @Test
    void testOverlapRule_RejectsRequestOverlappingSameEmployeeApprovedLeave() {
        // Arrange: Alice has an approved leave from 20 to 22 July (Monday - Wednesday).
        LeaveRequest approvedRequest = new LeaveRequest();
        approvedRequest.setId(101L);
        approvedRequest.setEmployee(alice);
        approvedRequest.setStartDate(LocalDate.of(2026, 7, 20));
        approvedRequest.setEndDate(LocalDate.of(2026, 7, 22));
        approvedRequest.setStatus(LeaveStatus.APPROVED);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(alice));
        // Overlap query returns the approved request
        when(leaveRequestRepository.findByEmployeeAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(alice), eq(LeaveStatus.APPROVED),
                eq(LocalDate.of(2026, 7, 23)),  // endDate of new request
                eq(LocalDate.of(2026, 7, 21))   // startDate of new request
        )).thenReturn(List.of(approvedRequest));

        // Act & Assert: submit a new request that overlaps -> exception
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> leaveRequestService.submitLeaveRequest(
                        1L,
                        LocalDate.of(2026, 7, 21),
                        LocalDate.of(2026, 7, 23)
                )
        );
        assertTrue(exception.getMessage().contains("Request overlap exception"));
    }

    // ============================================================
    // 2. 30% RULE TEST (required) – rejects when team limit exceeded
    // ============================================================
    @Test
    void testThirtyPercentRule_RejectsRequestExceedingTeamDailyLimit() {
        // Arrange: Team ENGINEERING has 4 members (we'll mock countByTeam to return 4)
        // Max allowed = floor(4 * 0.30) = 1. (Math.max makes it 1)
        // Bob already approved on 24 July (Friday).
        LeaveRequest bobsApproved = new LeaveRequest();
        bobsApproved.setId(201L);
        bobsApproved.setEmployee(bob);
        bobsApproved.setStartDate(LocalDate.of(2026, 7, 24));
        bobsApproved.setEndDate(LocalDate.of(2026, 7, 24));
        bobsApproved.setStatus(LeaveStatus.APPROVED);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(employeeRepository.countByTeam(Team.ENGINEERING)).thenReturn(4L);

        // No overlap for Alice (she doesn't have any approved leave)
        when(leaveRequestRepository.findByEmployeeAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                any(), any(), any(), any()
        )).thenReturn(Collections.emptyList());

        // Active approved leaves query returns Bob's leave for that day (Friday)
        when(leaveRequestRepository.findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(LeaveStatus.APPROVED),
                eq(LocalDate.of(2026, 7, 24)),
                eq(LocalDate.of(2026, 7, 24))
        )).thenReturn(List.of(bobsApproved));

        // Create a pending request for Alice on Friday
        LeaveRequest alicePending = new LeaveRequest();
        alicePending.setId(202L);
        alicePending.setEmployee(alice);
        alicePending.setStartDate(LocalDate.of(2026, 7, 24));
        alicePending.setEndDate(LocalDate.of(2026, 7, 24));
        alicePending.setStatus(LeaveStatus.PENDING);

        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(alicePending);

        // Submit the request
        LeaveRequest submitted = leaveRequestService.submitLeaveRequest(
                1L,
                LocalDate.of(2026, 7, 24),
                LocalDate.of(2026, 7, 24)
        );

        // Mock findById for the pending request when evaluate is called
        when(leaveRequestRepository.findById(202L)).thenReturn(Optional.of(alicePending));

        // Act & Assert: evaluate (approve) should throw 30% violation
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> leaveRequestService.evaluateLeaveRequest(202L, LeaveStatus.APPROVED)
        );
        assertTrue(exception.getMessage().contains("30% Boundary Rule Exception"));
    }

    // ============================================================
    // 3. BONUS: 30% rule accepts when under limit
    // ============================================================
    @Test
    void testThirtyPercentRule_AcceptsWhenWithinLimit() {
        // Thursday, 23 July (Weekday). No approved leaves.
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(employeeRepository.countByTeam(Team.ENGINEERING)).thenReturn(4L);

        // No overlap
        when(leaveRequestRepository.findByEmployeeAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                any(), any(), any(), any()
        )).thenReturn(Collections.emptyList());

        // No active approved leaves for that day
        when(leaveRequestRepository.findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(LeaveStatus.APPROVED),
                eq(LocalDate.of(2026, 7, 23)),
                eq(LocalDate.of(2026, 7, 23))
        )).thenReturn(Collections.emptyList());

        // Create pending request
        LeaveRequest pending = new LeaveRequest();
        pending.setId(301L);
        pending.setEmployee(alice);
        pending.setStartDate(LocalDate.of(2026, 7, 23));
        pending.setEndDate(LocalDate.of(2026, 7, 23));
        pending.setStatus(LeaveStatus.PENDING);

        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(pending);
        when(leaveRequestRepository.findById(301L)).thenReturn(Optional.of(pending));

        // Submit and approve – should not throw
        leaveRequestService.submitLeaveRequest(
                1L,
                LocalDate.of(2026, 7, 23),
                LocalDate.of(2026, 7, 23)
        );

        assertDoesNotThrow(() -> {
            leaveRequestService.evaluateLeaveRequest(301L, LeaveStatus.APPROVED);
        });
    }

    // ============================================================
    // 4. BONUS: 30% rule only counts same team
    // ============================================================
    @Test
    void testThirtyPercentRule_OnlyCountsSameTeam() {
        // Monday, 27 July (Weekday). Eve (OPERATIONS) has approved leave.
        Employee eve = new Employee();
        eve.setId(5L);
        eve.setName("Eve");
        eve.setTeam(Team.OPERATIONS);

        LeaveRequest evesApproved = new LeaveRequest();
        evesApproved.setId(401L);
        evesApproved.setEmployee(eve);
        evesApproved.setStartDate(LocalDate.of(2026, 7, 27));
        evesApproved.setEndDate(LocalDate.of(2026, 7, 27));
        evesApproved.setStatus(LeaveStatus.APPROVED);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(employeeRepository.countByTeam(Team.ENGINEERING)).thenReturn(4L);

        // No overlap for Alice
        when(leaveRequestRepository.findByEmployeeAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                any(), any(), any(), any()
        )).thenReturn(Collections.emptyList());

        // Active approved leaves query returns Eve's leave (different team)
        when(leaveRequestRepository.findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(LeaveStatus.APPROVED),
                eq(LocalDate.of(2026, 7, 27)),
                eq(LocalDate.of(2026, 7, 27))
        )).thenReturn(List.of(evesApproved));

        // Create pending request for Alice
        LeaveRequest pending = new LeaveRequest();
        pending.setId(402L);
        pending.setEmployee(alice);
        pending.setStartDate(LocalDate.of(2026, 7, 27));
        pending.setEndDate(LocalDate.of(2026, 7, 27));
        pending.setStatus(LeaveStatus.PENDING);

        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(pending);
        when(leaveRequestRepository.findById(402L)).thenReturn(Optional.of(pending));

        // Submit and approve – should succeed because Eve is in a different team.
        leaveRequestService.submitLeaveRequest(
                1L,
                LocalDate.of(2026, 7, 27),
                LocalDate.of(2026, 7, 27)
        );

        assertDoesNotThrow(() -> {
            leaveRequestService.evaluateLeaveRequest(402L, LeaveStatus.APPROVED);
        });
    }
}