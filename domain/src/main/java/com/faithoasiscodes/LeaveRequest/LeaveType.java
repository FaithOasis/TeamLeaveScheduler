package com.faithoasiscodes.LeaveRequest;

public enum LeaveType {
    ANNUAL("Annual Leave"),
    SICK("Sick Leave"),
    PERSONAL("Personal Leave"),
    OTHER("Other");

    private final String displayName;

    LeaveType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
