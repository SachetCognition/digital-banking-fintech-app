package com.yourorg.banking.loan.model;

public enum EmploymentStatus {
    EMPLOYED("Employed", "Full-time employment"),
    SELF_EMPLOYED("Self-Employed", "Self-employed or business owner"),
    UNEMPLOYED("Unemployed", "Currently unemployed"),
    RETIRED("Retired", "Retired from employment"),
    STUDENT("Student", "Currently studying"),
    PART_TIME("Part-Time", "Part-time employment"),
    CONTRACT("Contract", "Contract or temporary employment"),
    FREELANCE("Freelance", "Freelance or gig work");
    
    private final String displayName;
    private final String description;
    
    EmploymentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}

