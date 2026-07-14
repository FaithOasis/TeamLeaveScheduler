package com.faithoasiscodes.LeaveRequest;

import com.faithoasiscodes.Employee.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
// Make sure it says <LeaveRequest, Long> here!
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByStatus(LeaveStatus status);

    List<LeaveRequest> findByEmployeeAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Employee employee, LeaveStatus status, LocalDate endDate, LocalDate startDate
    );

    List<LeaveRequest> findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LeaveStatus status, LocalDate endDate, LocalDate startDate
    );
}