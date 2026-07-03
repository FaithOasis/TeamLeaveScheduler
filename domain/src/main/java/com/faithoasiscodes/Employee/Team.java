package com.faithoasiscodes.Employee;

public enum Team {
    ENGINEERING("Engineering"),
    OPERATIONS("Operations"),
    FINANCE("Finance");

    private final String displayName;

    Team(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
