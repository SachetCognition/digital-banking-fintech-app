package com.yourorg.banking.payments.model;

import java.math.BigDecimal;

public class WpsSifRecord {

    private String employeeId;
    private String employeeName;
    private String bankRoutingCode;
    private String accountNumber;
    private BigDecimal salaryAmount;
    private BigDecimal allowances;
    private BigDecimal deductions;
    private BigDecimal netSalary;

    public WpsSifRecord() {
    }

    public WpsSifRecord(String employeeId, String employeeName, String bankRoutingCode,
                        String accountNumber, BigDecimal salaryAmount, BigDecimal allowances,
                        BigDecimal deductions, BigDecimal netSalary) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.bankRoutingCode = bankRoutingCode;
        this.accountNumber = accountNumber;
        this.salaryAmount = salaryAmount;
        this.allowances = allowances;
        this.deductions = deductions;
        this.netSalary = netSalary;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getBankRoutingCode() {
        return bankRoutingCode;
    }

    public void setBankRoutingCode(String bankRoutingCode) {
        this.bankRoutingCode = bankRoutingCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getSalaryAmount() {
        return salaryAmount;
    }

    public void setSalaryAmount(BigDecimal salaryAmount) {
        this.salaryAmount = salaryAmount;
    }

    public BigDecimal getAllowances() {
        return allowances;
    }

    public void setAllowances(BigDecimal allowances) {
        this.allowances = allowances;
    }

    public BigDecimal getDeductions() {
        return deductions;
    }

    public void setDeductions(BigDecimal deductions) {
        this.deductions = deductions;
    }

    public BigDecimal getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(BigDecimal netSalary) {
        this.netSalary = netSalary;
    }

    @Override
    public String toString() {
        return String.join("|",
                employeeId,
                employeeName,
                bankRoutingCode,
                accountNumber,
                salaryAmount.toPlainString(),
                allowances.toPlainString(),
                deductions.toPlainString(),
                netSalary.toPlainString());
    }
}
