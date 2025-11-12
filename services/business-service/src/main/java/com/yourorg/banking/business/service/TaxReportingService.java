package com.yourorg.banking.business.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TaxReportingService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Generate tax documents for a customer
     */
    @Transactional
    public TaxDocument generateTaxDocument(String customerId, int taxYear, String documentType) {
        TaxDocument document = new TaxDocument();
        document.setCustomerId(customerId);
        document.setTaxYear(taxYear);
        document.setDocumentType(documentType);
        document.setDocumentNumber(generateDocumentNumber(documentType, taxYear));
        document.setDocumentStatus("generating");
        document.setGeneratedAt(LocalDateTime.now());

        try {
            // Calculate tax amounts based on document type
            TaxAmounts taxAmounts = calculateTaxAmounts(customerId, taxYear, documentType);
            
            document.setTotalInterest(taxAmounts.getTotalInterest());
            document.setTotalDividends(taxAmounts.getTotalDividends());
            document.setTotalCapitalGains(taxAmounts.getTotalCapitalGains());
            document.setTotalOtherIncome(taxAmounts.getTotalOtherIncome());
            document.setFederalTaxWithheld(taxAmounts.getFederalTaxWithheld());
            document.setStateTaxWithheld(taxAmounts.getStateTaxWithheld());

            // Generate document items
            List<TaxDocumentItem> documentItems = generateDocumentItems(customerId, taxYear, documentType);
            document.setDocumentItems(documentItems);

            // Record tax document
            recordTaxDocument(document);

            // Generate PDF document
            String filePath = generateTaxDocumentPDF(document);
            document.setFilePath(filePath);

            document.setDocumentStatus("generated");

        } catch (Exception e) {
            document.setDocumentStatus("error");
            document.setErrorMessage("Failed to generate tax document: " + e.getMessage());
        }

        return document;
    }

    /**
     * Get tax documents for customer
     */
    public List<TaxDocument> getTaxDocuments(String customerId, int taxYear) {
        String sql = """
            SELECT * FROM tax_documents 
            WHERE customer_id = ? AND tax_year = ?
            ORDER BY document_type, generated_at DESC
            """;
        
        return jdbcTemplate.query(sql, new Object[]{customerId, taxYear}, (rs, rowNum) -> {
            TaxDocument document = new TaxDocument();
            document.setId(rs.getString("id"));
            document.setCustomerId(rs.getString("customer_id"));
            document.setTaxYear(rs.getInt("tax_year"));
            document.setDocumentType(rs.getString("document_type"));
            document.setDocumentNumber(rs.getString("document_number"));
            document.setTotalInterest(rs.getBigDecimal("total_interest"));
            document.setTotalDividends(rs.getBigDecimal("total_dividends"));
            document.setTotalCapitalGains(rs.getBigDecimal("total_capital_gains"));
            document.setTotalOtherIncome(rs.getBigDecimal("total_other_income"));
            document.setFederalTaxWithheld(rs.getBigDecimal("federal_tax_withheld"));
            document.setStateTaxWithheld(rs.getBigDecimal("state_tax_withheld"));
            document.setDocumentStatus(rs.getString("document_status"));
            document.setGeneratedAt(rs.getTimestamp("generated_at") != null ? 
                rs.getTimestamp("generated_at").toLocalDateTime() : null);
            document.setMailedAt(rs.getTimestamp("mailed_at") != null ? 
                rs.getTimestamp("mailed_at").toLocalDateTime() : null);
            document.setDeliveredAt(rs.getTimestamp("delivered_at") != null ? 
                rs.getTimestamp("delivered_at").toLocalDateTime() : null);
            document.setFilePath(rs.getString("file_path"));
            return document;
        });
    }

    /**
     * Mail tax document
     */
    @Transactional
    public TaxDocumentMailing mailTaxDocument(String documentId) {
        TaxDocumentMailing mailing = new TaxDocumentMailing();
        mailing.setDocumentId(documentId);
        mailing.setStatus("mailing");
        mailing.setStartedAt(LocalDateTime.now());

        try {
            // Get tax document
            TaxDocument document = getTaxDocument(documentId);
            if (document == null) {
                mailing.setStatus("failed");
                mailing.setErrorMessage("Tax document not found");
                return mailing;
            }

            // Send email notification
            sendTaxDocumentEmail(document);

            // Update document status
            updateTaxDocumentStatus(documentId, "mailed", LocalDateTime.now());

            mailing.setStatus("mailed");
            mailing.setMailedAt(LocalDateTime.now());
            mailing.setDocumentNumber(document.getDocumentNumber());

        } catch (Exception e) {
            mailing.setStatus("failed");
            mailing.setErrorMessage(e.getMessage());
        }

        return mailing;
    }

    /**
     * Get tax summary for customer
     */
    public TaxSummary getTaxSummary(String customerId, int taxYear) {
        TaxSummary summary = new TaxSummary();
        summary.setCustomerId(customerId);
        summary.setTaxYear(taxYear);
        summary.setGeneratedAt(LocalDateTime.now());

        try {
            String sql = """
                SELECT 
                    document_type,
                    COUNT(*) as document_count,
                    SUM(total_interest) as total_interest,
                    SUM(total_dividends) as total_dividends,
                    SUM(total_capital_gains) as total_capital_gains,
                    SUM(total_other_income) as total_other_income,
                    SUM(federal_tax_withheld) as total_federal_withheld,
                    SUM(state_tax_withheld) as total_state_withheld
                FROM tax_documents 
                WHERE customer_id = ? AND tax_year = ?
                GROUP BY document_type
                """;
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, customerId, taxYear);
            
            List<DocumentTypeSummary> documentTypeSummaries = new ArrayList<>();
            BigDecimal totalInterest = BigDecimal.ZERO;
            BigDecimal totalDividends = BigDecimal.ZERO;
            BigDecimal totalCapitalGains = BigDecimal.ZERO;
            BigDecimal totalOtherIncome = BigDecimal.ZERO;
            BigDecimal totalFederalWithheld = BigDecimal.ZERO;
            BigDecimal totalStateWithheld = BigDecimal.ZERO;
            
            for (Map<String, Object> row : results) {
                DocumentTypeSummary docSummary = new DocumentTypeSummary();
                docSummary.setDocumentType((String) row.get("document_type"));
                docSummary.setDocumentCount(((Number) row.get("document_count")).intValue());
                docSummary.setTotalInterest(((BigDecimal) row.get("total_interest")));
                docSummary.setTotalDividends(((BigDecimal) row.get("total_dividends")));
                docSummary.setTotalCapitalGains(((BigDecimal) row.get("total_capital_gains")));
                docSummary.setTotalOtherIncome(((BigDecimal) row.get("total_other_income")));
                docSummary.setTotalFederalWithheld(((BigDecimal) row.get("total_federal_withheld")));
                docSummary.setTotalStateWithheld(((BigDecimal) row.get("total_state_withheld")));
                
                documentTypeSummaries.add(docSummary);
                totalInterest = totalInterest.add(docSummary.getTotalInterest());
                totalDividends = totalDividends.add(docSummary.getTotalDividends());
                totalCapitalGains = totalCapitalGains.add(docSummary.getTotalCapitalGains());
                totalOtherIncome = totalOtherIncome.add(docSummary.getTotalOtherIncome());
                totalFederalWithheld = totalFederalWithheld.add(docSummary.getTotalFederalWithheld());
                totalStateWithheld = totalStateWithheld.add(docSummary.getTotalStateWithheld());
            }
            
            summary.setDocumentTypeSummaries(documentTypeSummaries);
            summary.setTotalInterest(totalInterest);
            summary.setTotalDividends(totalDividends);
            summary.setTotalCapitalGains(totalCapitalGains);
            summary.setTotalOtherIncome(totalOtherIncome);
            summary.setTotalFederalWithheld(totalFederalWithheld);
            summary.setTotalStateWithheld(totalStateWithheld);
            summary.setTotalIncome(totalInterest.add(totalDividends).add(totalCapitalGains).add(totalOtherIncome));
            summary.setTotalTaxWithheld(totalFederalWithheld.add(totalStateWithheld));

        } catch (Exception e) {
            summary.setErrorMessage("Failed to generate tax summary: " + e.getMessage());
        }

        return summary;
    }

    /**
     * Schedule tax document generation
     */
    public TaxDocumentSchedule scheduleTaxDocumentGeneration(int taxYear) {
        TaxDocumentSchedule schedule = new TaxDocumentSchedule();
        schedule.setTaxYear(taxYear);
        schedule.setStatus("scheduled");
        schedule.setScheduledAt(LocalDateTime.now());

        try {
            // Get all customers who need tax documents
            List<String> customerIds = getCustomersForTaxDocuments(taxYear);
            
            schedule.setCustomerCount(customerIds.size());
            schedule.setEstimatedCompletionDate(LocalDateTime.now().plusDays(7));

            // Schedule generation for each customer
            for (String customerId : customerIds) {
                scheduleTaxDocumentForCustomer(customerId, taxYear);
            }

            schedule.setStatus("completed");

        } catch (Exception e) {
            schedule.setStatus("failed");
            schedule.setErrorMessage(e.getMessage());
        }

        return schedule;
    }

    // Private helper methods
    private TaxAmounts calculateTaxAmounts(String customerId, int taxYear, String documentType) {
        TaxAmounts amounts = new TaxAmounts();
        
        try {
            // Calculate interest income
            String interestSql = """
                SELECT COALESCE(SUM(monthly_interest), 0) 
                FROM interest_calculations 
                WHERE customer_id = ? 
                AND EXTRACT(YEAR FROM calculation_date) = ?
                AND is_processed = true
                """;
            BigDecimal totalInterest = jdbcTemplate.queryForObject(interestSql, 
                new Object[]{customerId, taxYear}, BigDecimal.class);
            amounts.setTotalInterest(totalInterest);

            // Calculate dividends (placeholder - would come from investment service)
            amounts.setTotalDividends(BigDecimal.ZERO);

            // Calculate capital gains (placeholder - would come from investment service)
            amounts.setTotalCapitalGains(BigDecimal.ZERO);

            // Calculate other income (placeholder)
            amounts.setTotalOtherIncome(BigDecimal.ZERO);

            // Calculate tax withheld (placeholder - would be calculated based on rates)
            amounts.setFederalTaxWithheld(totalInterest.multiply(new BigDecimal("0.10")));
            amounts.setStateTaxWithheld(totalInterest.multiply(new BigDecimal("0.05")));

        } catch (Exception e) {
            amounts.setErrorMessage("Failed to calculate tax amounts: " + e.getMessage());
        }

        return amounts;
    }

    private List<TaxDocumentItem> generateDocumentItems(String customerId, int taxYear, String documentType) {
        List<TaxDocumentItem> items = new ArrayList<>();
        
        try {
            // Get account types for customer
            String accountSql = """
                SELECT DISTINCT account_type 
                FROM interest_calculations 
                WHERE customer_id = ? 
                AND EXTRACT(YEAR FROM calculation_date) = ?
                AND is_processed = true
                """;
            
            List<String> accountTypes = jdbcTemplate.queryForList(accountSql, 
                new Object[]{customerId, taxYear}, String.class);
            
            for (String accountType : accountTypes) {
                TaxDocumentItem item = new TaxDocumentItem();
                item.setAccountType(accountType);
                
                // Calculate amounts for this account type
                String amountsSql = """
                    SELECT 
                        COALESCE(SUM(monthly_interest), 0) as interest_income,
                        COALESCE(SUM(monthly_interest * 0.10), 0) as federal_withholding,
                        COALESCE(SUM(monthly_interest * 0.05), 0) as state_withholding
                    FROM interest_calculations 
                    WHERE customer_id = ? 
                    AND account_type = ?
                    AND EXTRACT(YEAR FROM calculation_date) = ?
                    AND is_processed = true
                    """;
                
                Map<String, Object> result = jdbcTemplate.queryForMap(amountsSql, 
                    customerId, accountType, taxYear);
                
                item.setInterestIncome(((BigDecimal) result.get("interest_income")));
                item.setDividendIncome(BigDecimal.ZERO);
                item.setCapitalGains(BigDecimal.ZERO);
                item.setOtherIncome(BigDecimal.ZERO);
                item.setFederalWithholding(((BigDecimal) result.get("federal_withholding")));
                item.setStateWithholding(((BigDecimal) result.get("state_withholding")));
                
                items.add(item);
            }

        } catch (Exception e) {
            // Handle error
        }

        return items;
    }

    private String generateDocumentNumber(String documentType, int taxYear) {
        return documentType + "-" + taxYear + "-" + System.currentTimeMillis();
    }

    private String generateTaxDocumentPDF(TaxDocument document) {
        // In a real implementation, this would generate a PDF using iText
        return "/tax-documents/" + document.getDocumentNumber() + ".pdf";
    }

    private void sendTaxDocumentEmail(TaxDocument document) {
        // In a real implementation, this would send an email notification
        System.out.println("Tax document email sent for: " + document.getDocumentNumber());
    }

    private List<String> getCustomersForTaxDocuments(int taxYear) {
        String sql = """
            SELECT DISTINCT customer_id 
            FROM interest_calculations 
            WHERE EXTRACT(YEAR FROM calculation_date) = ?
            AND is_processed = true
            """;
        
        return jdbcTemplate.queryForList(sql, new Object[]{taxYear}, String.class);
    }

    private void scheduleTaxDocumentForCustomer(String customerId, int taxYear) {
        // In a real implementation, this would schedule the generation
        System.out.println("Scheduled tax document generation for customer: " + customerId);
    }

    private TaxDocument getTaxDocument(String documentId) {
        String sql = "SELECT * FROM tax_documents WHERE id = ?";
        
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{documentId}, (rs, rowNum) -> {
                TaxDocument document = new TaxDocument();
                document.setId(rs.getString("id"));
                document.setCustomerId(rs.getString("customer_id"));
                document.setTaxYear(rs.getInt("tax_year"));
                document.setDocumentType(rs.getString("document_type"));
                document.setDocumentNumber(rs.getString("document_number"));
                document.setTotalInterest(rs.getBigDecimal("total_interest"));
                document.setTotalDividends(rs.getBigDecimal("total_dividends"));
                document.setTotalCapitalGains(rs.getBigDecimal("total_capital_gains"));
                document.setTotalOtherIncome(rs.getBigDecimal("total_other_income"));
                document.setFederalTaxWithheld(rs.getBigDecimal("federal_tax_withheld"));
                document.setStateTaxWithheld(rs.getBigDecimal("state_tax_withheld"));
                document.setDocumentStatus(rs.getString("document_status"));
                return document;
            });
        } catch (Exception e) {
            return null;
        }
    }

    private void recordTaxDocument(TaxDocument document) {
        String sql = """
            INSERT INTO tax_documents 
            (id, customer_id, tax_year, document_type, document_number, total_interest, 
             total_dividends, total_capital_gains, total_other_income, federal_tax_withheld, 
             state_tax_withheld, document_status, generated_at, file_path)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            UUID.randomUUID().toString(),
            document.getCustomerId(),
            document.getTaxYear(),
            document.getDocumentType(),
            document.getDocumentNumber(),
            document.getTotalInterest(),
            document.getTotalDividends(),
            document.getTotalCapitalGains(),
            document.getTotalOtherIncome(),
            document.getFederalTaxWithheld(),
            document.getStateTaxWithheld(),
            document.getDocumentStatus(),
            document.getGeneratedAt(),
            document.getFilePath()
        );

        // Record document items
        for (TaxDocumentItem item : document.getDocumentItems()) {
            recordTaxDocumentItem(document.getId(), item);
        }
    }

    private void recordTaxDocumentItem(String documentId, TaxDocumentItem item) {
        String sql = """
            INSERT INTO tax_document_items 
            (id, tax_document_id, account_type, interest_income, dividend_income, 
             capital_gains, other_income, federal_withholding, state_withholding)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            UUID.randomUUID().toString(),
            documentId,
            item.getAccountType(),
            item.getInterestIncome(),
            item.getDividendIncome(),
            item.getCapitalGains(),
            item.getOtherIncome(),
            item.getFederalWithholding(),
            item.getStateWithholding()
        );
    }

    private void updateTaxDocumentStatus(String documentId, String status, LocalDateTime timestamp) {
        String sql = "UPDATE tax_documents SET document_status = ?, mailed_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, timestamp, documentId);
    }

    // Data classes
    public static class TaxDocument {
        private String id;
        private String customerId;
        private int taxYear;
        private String documentType;
        private String documentNumber;
        private BigDecimal totalInterest;
        private BigDecimal totalDividends;
        private BigDecimal totalCapitalGains;
        private BigDecimal totalOtherIncome;
        private BigDecimal federalTaxWithheld;
        private BigDecimal stateTaxWithheld;
        private String documentStatus;
        private LocalDateTime generatedAt;
        private LocalDateTime mailedAt;
        private LocalDateTime deliveredAt;
        private String filePath;
        private List<TaxDocumentItem> documentItems;
        private String errorMessage;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public int getTaxYear() { return taxYear; }
        public void setTaxYear(int taxYear) { this.taxYear = taxYear; }
        public String getDocumentType() { return documentType; }
        public void setDocumentType(String documentType) { this.documentType = documentType; }
        public String getDocumentNumber() { return documentNumber; }
        public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
        public BigDecimal getTotalInterest() { return totalInterest; }
        public void setTotalInterest(BigDecimal totalInterest) { this.totalInterest = totalInterest; }
        public BigDecimal getTotalDividends() { return totalDividends; }
        public void setTotalDividends(BigDecimal totalDividends) { this.totalDividends = totalDividends; }
        public BigDecimal getTotalCapitalGains() { return totalCapitalGains; }
        public void setTotalCapitalGains(BigDecimal totalCapitalGains) { this.totalCapitalGains = totalCapitalGains; }
        public BigDecimal getTotalOtherIncome() { return totalOtherIncome; }
        public void setTotalOtherIncome(BigDecimal totalOtherIncome) { this.totalOtherIncome = totalOtherIncome; }
        public BigDecimal getFederalTaxWithheld() { return federalTaxWithheld; }
        public void setFederalTaxWithheld(BigDecimal federalTaxWithheld) { this.federalTaxWithheld = federalTaxWithheld; }
        public BigDecimal getStateTaxWithheld() { return stateTaxWithheld; }
        public void setStateTaxWithheld(BigDecimal stateTaxWithheld) { this.stateTaxWithheld = stateTaxWithheld; }
        public String getDocumentStatus() { return documentStatus; }
        public void setDocumentStatus(String documentStatus) { this.documentStatus = documentStatus; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
        public LocalDateTime getMailedAt() { return mailedAt; }
        public void setMailedAt(LocalDateTime mailedAt) { this.mailedAt = mailedAt; }
        public LocalDateTime getDeliveredAt() { return deliveredAt; }
        public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public List<TaxDocumentItem> getDocumentItems() { return documentItems; }
        public void setDocumentItems(List<TaxDocumentItem> documentItems) { this.documentItems = documentItems; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class TaxDocumentItem {
        private String accountType;
        private BigDecimal interestIncome;
        private BigDecimal dividendIncome;
        private BigDecimal capitalGains;
        private BigDecimal otherIncome;
        private BigDecimal federalWithholding;
        private BigDecimal stateWithholding;

        // Getters and setters
        public String getAccountType() { return accountType; }
        public void setAccountType(String accountType) { this.accountType = accountType; }
        public BigDecimal getInterestIncome() { return interestIncome; }
        public void setInterestIncome(BigDecimal interestIncome) { this.interestIncome = interestIncome; }
        public BigDecimal getDividendIncome() { return dividendIncome; }
        public void setDividendIncome(BigDecimal dividendIncome) { this.dividendIncome = dividendIncome; }
        public BigDecimal getCapitalGains() { return capitalGains; }
        public void setCapitalGains(BigDecimal capitalGains) { this.capitalGains = capitalGains; }
        public BigDecimal getOtherIncome() { return otherIncome; }
        public void setOtherIncome(BigDecimal otherIncome) { this.otherIncome = otherIncome; }
        public BigDecimal getFederalWithholding() { return federalWithholding; }
        public void setFederalWithholding(BigDecimal federalWithholding) { this.federalWithholding = federalWithholding; }
        public BigDecimal getStateWithholding() { return stateWithholding; }
        public void setStateWithholding(BigDecimal stateWithholding) { this.stateWithholding = stateWithholding; }
    }

    public static class TaxAmounts {
        private BigDecimal totalInterest;
        private BigDecimal totalDividends;
        private BigDecimal totalCapitalGains;
        private BigDecimal totalOtherIncome;
        private BigDecimal federalTaxWithheld;
        private BigDecimal stateTaxWithheld;
        private String errorMessage;

        // Getters and setters
        public BigDecimal getTotalInterest() { return totalInterest; }
        public void setTotalInterest(BigDecimal totalInterest) { this.totalInterest = totalInterest; }
        public BigDecimal getTotalDividends() { return totalDividends; }
        public void setTotalDividends(BigDecimal totalDividends) { this.totalDividends = totalDividends; }
        public BigDecimal getTotalCapitalGains() { return totalCapitalGains; }
        public void setTotalCapitalGains(BigDecimal totalCapitalGains) { this.totalCapitalGains = totalCapitalGains; }
        public BigDecimal getTotalOtherIncome() { return totalOtherIncome; }
        public void setTotalOtherIncome(BigDecimal totalOtherIncome) { this.totalOtherIncome = totalOtherIncome; }
        public BigDecimal getFederalTaxWithheld() { return federalTaxWithheld; }
        public void setFederalTaxWithheld(BigDecimal federalTaxWithheld) { this.federalTaxWithheld = federalTaxWithheld; }
        public BigDecimal getStateTaxWithheld() { return stateTaxWithheld; }
        public void setStateTaxWithheld(BigDecimal stateTaxWithheld) { this.stateTaxWithheld = stateTaxWithheld; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class TaxDocumentMailing {
        private String documentId;
        private String status;
        private LocalDateTime startedAt;
        private LocalDateTime mailedAt;
        private String documentNumber;
        private String errorMessage;

        // Getters and setters
        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getStartedAt() { return startedAt; }
        public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
        public LocalDateTime getMailedAt() { return mailedAt; }
        public void setMailedAt(LocalDateTime mailedAt) { this.mailedAt = mailedAt; }
        public String getDocumentNumber() { return documentNumber; }
        public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class TaxSummary {
        private String customerId;
        private int taxYear;
        private LocalDateTime generatedAt;
        private List<DocumentTypeSummary> documentTypeSummaries;
        private BigDecimal totalInterest;
        private BigDecimal totalDividends;
        private BigDecimal totalCapitalGains;
        private BigDecimal totalOtherIncome;
        private BigDecimal totalIncome;
        private BigDecimal totalFederalWithheld;
        private BigDecimal totalStateWithheld;
        private BigDecimal totalTaxWithheld;
        private String errorMessage;

        // Getters and setters
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public int getTaxYear() { return taxYear; }
        public void setTaxYear(int taxYear) { this.taxYear = taxYear; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
        public List<DocumentTypeSummary> getDocumentTypeSummaries() { return documentTypeSummaries; }
        public void setDocumentTypeSummaries(List<DocumentTypeSummary> documentTypeSummaries) { this.documentTypeSummaries = documentTypeSummaries; }
        public BigDecimal getTotalInterest() { return totalInterest; }
        public void setTotalInterest(BigDecimal totalInterest) { this.totalInterest = totalInterest; }
        public BigDecimal getTotalDividends() { return totalDividends; }
        public void setTotalDividends(BigDecimal totalDividends) { this.totalDividends = totalDividends; }
        public BigDecimal getTotalCapitalGains() { return totalCapitalGains; }
        public void setTotalCapitalGains(BigDecimal totalCapitalGains) { this.totalCapitalGains = totalCapitalGains; }
        public BigDecimal getTotalOtherIncome() { return totalOtherIncome; }
        public void setTotalOtherIncome(BigDecimal totalOtherIncome) { this.totalOtherIncome = totalOtherIncome; }
        public BigDecimal getTotalIncome() { return totalIncome; }
        public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
        public BigDecimal getTotalFederalWithheld() { return totalFederalWithheld; }
        public void setTotalFederalWithheld(BigDecimal totalFederalWithheld) { this.totalFederalWithheld = totalFederalWithheld; }
        public BigDecimal getTotalStateWithheld() { return totalStateWithheld; }
        public void setTotalStateWithheld(BigDecimal totalStateWithheld) { this.totalStateWithheld = totalStateWithheld; }
        public BigDecimal getTotalTaxWithheld() { return totalTaxWithheld; }
        public void setTotalTaxWithheld(BigDecimal totalTaxWithheld) { this.totalTaxWithheld = totalTaxWithheld; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class DocumentTypeSummary {
        private String documentType;
        private int documentCount;
        private BigDecimal totalInterest;
        private BigDecimal totalDividends;
        private BigDecimal totalCapitalGains;
        private BigDecimal totalOtherIncome;
        private BigDecimal totalFederalWithheld;
        private BigDecimal totalStateWithheld;

        // Getters and setters
        public String getDocumentType() { return documentType; }
        public void setDocumentType(String documentType) { this.documentType = documentType; }
        public int getDocumentCount() { return documentCount; }
        public void setDocumentCount(int documentCount) { this.documentCount = documentCount; }
        public BigDecimal getTotalInterest() { return totalInterest; }
        public void setTotalInterest(BigDecimal totalInterest) { this.totalInterest = totalInterest; }
        public BigDecimal getTotalDividends() { return totalDividends; }
        public void setTotalDividends(BigDecimal totalDividends) { this.totalDividends = totalDividends; }
        public BigDecimal getTotalCapitalGains() { return totalCapitalGains; }
        public void setTotalCapitalGains(BigDecimal totalCapitalGains) { this.totalCapitalGains = totalCapitalGains; }
        public BigDecimal getTotalOtherIncome() { return totalOtherIncome; }
        public void setTotalOtherIncome(BigDecimal totalOtherIncome) { this.totalOtherIncome = totalOtherIncome; }
        public BigDecimal getTotalFederalWithheld() { return totalFederalWithheld; }
        public void setTotalFederalWithheld(BigDecimal totalFederalWithheld) { this.totalFederalWithheld = totalFederalWithheld; }
        public BigDecimal getTotalStateWithheld() { return totalStateWithheld; }
        public void setTotalStateWithheld(BigDecimal totalStateWithheld) { this.totalStateWithheld = totalStateWithheld; }
    }

    public static class TaxDocumentSchedule {
        private int taxYear;
        private String status;
        private LocalDateTime scheduledAt;
        private int customerCount;
        private LocalDateTime estimatedCompletionDate;
        private String errorMessage;

        // Getters and setters
        public int getTaxYear() { return taxYear; }
        public void setTaxYear(int taxYear) { this.taxYear = taxYear; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getScheduledAt() { return scheduledAt; }
        public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
        public int getCustomerCount() { return customerCount; }
        public void setCustomerCount(int customerCount) { this.customerCount = customerCount; }
        public LocalDateTime getEstimatedCompletionDate() { return estimatedCompletionDate; }
        public void setEstimatedCompletionDate(LocalDateTime estimatedCompletionDate) { this.estimatedCompletionDate = estimatedCompletionDate; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}

