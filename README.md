Team Leave Scheduler

A small internal HR tool that lets a manager view their team's leave calendar for the next 30 days, submit leave requests, and approve or reject pending requests — enforcing the 30% team-availability rule and the no-overlap rule.

Tech stack
    Backend: Java 17, Spring Boot 4.1.0, Spring Data JPA, Hibernate
    Database: SQLite (file-based, no separate DB server needed)
    Frontend: Plain HTML/CSS/JavaScript (no build step, no framework)
    Build tool: Maven (multi-module project)


Prerequisites
    JDK 17 or later — check with java -version
    Maven 3.9+ — check with mvn -version
    No database installation needed; SQLite is embedded and the .db file is created automatically on first run.


Project structure

This is a multi-module Maven project:

TeamLeaveScheduler/
├── pom.xml                      # parent/reactor pom
├── domain/                      # entities & enums
│   └── src/main/java/com/faithoasiscodes/
│       ├── Employee/            # Employee.java, Team.java
│       ├── LeaveRequest/        # LeaveRequest.java, LeaveStatus.java
│       └── PublicHoliday/       # PublicHoliday.java
├── persistence/                 # Spring Data JPA repositories
│   └── src/main/java/com/faithoasiscodes/
│       ├── Employee/            # EmployeeRepository.java
│       ├── LeaveRequest/        # LeaveRequestRepository.java
│       └── PublicHoliday/       # PublicHolidayRepository.java
├── service/                     # business logic
│   └── src/main/java/com/faithoasiscodes/LeaveRequest/
│       └── LeaveRequestService.java
├── api/                         # REST controllers
│   └── src/main/java/com/faithoasiscodes/LeaveRequest/
│       └── LeaveRequestController.java
└── app/                         # Spring Boot entry point, config, seed data, frontend, tests
├── src/main/java/com/faithoasiscodes/
│   ├── TeamLeaveSchedulerApplication.java
│   └── config/
│       └── DataSeeder.java
├── src/main/resources/
│   ├── application.properties
│   ├── employees.csv            # seed data — 15 employees across 3 teams
│   ├── public_holidays.json     # seed data — upcoming public holidays
│   └── static/
│       └── index.html           # the frontend
└── src/test/java/com/faithoasiscodes/LeaveRequest/
└── LeaveRequestServiceTestMockito.java

**Setup & running the app**
    
    
    Clone the repository and open a terminal in the project root (the folder containing the top-level pom.xml).
    Build all modules:
    
    
    bash   mvn clean install
    
    This compiles domain, persistence, service, api, and app in order and runs any tests.
    
    
    Run the application from the app module:
    
    
    bash   mvn spring-boot:run -pl app

    
    
    First run behavior: on startup, the app automatically:
    
    Creates the SQLite database file (teamleavescheduler.db) in the project root if it doesn't exist
    Creates all tables via Hibernate (spring.jpa.hibernate.ddl-auto=update)
    Seeds 15 employees from employees.csv (only if the employees table is empty)
    Seeds public holidays from public_holidays.json (only if the public_holidays table is empty)
    
    
    Seeding is idempotent — restarting the app won't duplicate rows.
    Open the app in a browser once you see Started TeamLeaveSchedulerApplication in the logs:
    

http://localhost:8080

#### **API endpoints**

    All endpoints are under /api/leaves.
    
    MethodPathDescriptionGET/api/leaves/employeesList all employeesPOST/api/leaves/employeesAdd an employee — query params: name, teamGET/api/leaves/pendingList all pending leave requestsGET/api/leaves/approvedList all approved leave requestsGET/api/leaves/calendarNext-30-day calendar grid (date → names on leave)POST/api/leaves/submitSubmit a leave request — query params: employeeId, startDate, endDate (ISO yyyy-MM-dd)POST/api/leaves/evaluateApprove/reject a request — query params: requestId, decision (APPROVED or REJECTED)
    
    team accepts: ENGINEERING, OPERATIONS, FINANCE.
    
    Business rules implemented


30% rule: No more than 30% of a team (rounded down, minimum 1) may be on approved leave on the same working day. Validated day-by-day across a multi-day request.
Weekends excluded: Saturdays and Sundays are never counted as working days.
Public holidays excluded: A public holiday within a leave range doesn't count against the day-by-day team-availability check.
No overlapping requests: A new request is rejected outright if it overlaps an existing approved request for the same employee.


See DECISIONS.md for the reasoning behind each ambiguous interpretation.

Running tests

bashmvn test

Tests cover the 30% rule and the overlap rule (see app/src/test/java/com/faithoasiscodes/LeaveRequest/LeaveRequestServiceTestMockito.java).

Troubleshooting

    Failed to determine a suitable driver class on startup: app/src/main/resources/application.properties is missing or not on the classpath. Confirm it exists and contains spring.datasource.url / spring.datasource.driver-class-name, then re-run mvn clean install.
    FileNotFoundException for employees.csv or public_holidays.json: confirm both files are in app/src/main/resources/ (not a different module) and rebuild.
    500 errors from /api/leaves/employees or /pending//approved: check that Employee.leaveRequests is annotated with @JsonIgnore — without it, Jackson will infinitely recurse through the Employee ↔ LeaveRequest relationship.
    Port 8080 already in use: stop whatever else is using it, or run with --server.port=8081.


Resetting the database
    Delete teamleavescheduler.db from the project root and restart the app — it will be recreated and reseeded from scratch.
