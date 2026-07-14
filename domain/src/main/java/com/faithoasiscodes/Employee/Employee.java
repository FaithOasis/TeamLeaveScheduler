package com.faithoasiscodes.Employee;

import com.faithoasiscodes.LeaveRequest.LeaveRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="employees")
@Getter
@Setter
@NoArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int leaveBalance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Team team;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<LeaveRequest> leaveRequests = new ArrayList<>();

}
