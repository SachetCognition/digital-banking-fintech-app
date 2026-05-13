#!/usr/bin/env python3
"""
JIRA Import Script — Emirates Digital Bank Middle East Compliance
=================================================================
Creates Epics, Stories, and Sub-tasks in the Sach_Sales JIRA project
using the JIRA REST API.

Usage:
  export JIRA_URL=https://your-instance.atlassian.net
  export JIRA_USER=your-email@example.com
  export JIRA_TOKEN=your-api-token
  python3 scripts/jira_import.py

If JIRA credentials are not set, the script writes the full payload to
scripts/jira-import-payload.json for manual import.
"""

import json
import os
import sys
import base64
from pathlib import Path

try:
    import requests
    HAS_REQUESTS = True
except ImportError:
    HAS_REQUESTS = False

PROJECT_KEY = "SACH"  # Sach_Sales project key — adjust if different

# ---------------------------------------------------------------------------
# Data definitions
# ---------------------------------------------------------------------------

WORKSTREAMS = [
    {
        "key": "WS1",
        "title": "WS1 — Islamic Finance Products",
        "label": "ws1-islamic-finance",
        "priority": "Critical",
        "requirements": [
            {
                "id": "REQ-IF-001",
                "summary": "Murabaha (cost-plus) financing with configurable markup rates",
                "description": (
                    "System shall support Murabaha (cost-plus) financing with configurable markup rates. "
                    "The bank purchases an asset and resells it to the customer at a disclosed profit margin; "
                    "payment may be deferred or in installments.\n\n"
                    "**Acceptance Criteria:** Given a Murabaha financing request with asset cost X and markup "
                    "rate R%, the system calculates total price = X * (1 + R/100) and generates an installment "
                    "schedule. The profit amount is disclosed to the customer before contract signing."
                ),
                "test_cases": ["TC-IF-001", "TC-IF-007"],
            },
            {
                "id": "REQ-IF-002",
                "summary": "Ijara (leasing) with rental schedule generation",
                "description": (
                    "System shall support Ijara (leasing) with rental schedule generation. The bank purchases "
                    "and leases an asset to the customer; ownership may transfer at end of lease.\n\n"
                    "**Acceptance Criteria:** Given an Ijara lease request, the system generates a rental "
                    "schedule with monthly payments for the lease term."
                ),
                "test_cases": ["TC-IF-003"],
            },
            {
                "id": "REQ-IF-003",
                "summary": "Diminishing Musharaka for home financing with equity split tracking",
                "description": (
                    "System shall support Diminishing Musharaka for home financing with equity split tracking. "
                    "The bank and customer co-own the property; the customer gradually buys out the bank's share.\n\n"
                    "**Acceptance Criteria:** Given a Diminishing Musharaka contract with initial equity split, "
                    "the system tracks equity reduction per payment period and recalculates rental payments."
                ),
                "test_cases": ["TC-IF-002"],
            },
            {
                "id": "REQ-IF-004",
                "summary": "Mudarabah profit-sharing investment accounts with configurable profit ratios",
                "description": (
                    "System shall support Mudarabah profit-sharing investment accounts with configurable profit "
                    "ratios between the bank (Mudarib) and the customer (Rabb-ul-Maal).\n\n"
                    "**Acceptance Criteria:** Given a Mudarabah investment with a profit-sharing ratio, the "
                    "system distributes realized profits according to the agreed ratio."
                ),
                "test_cases": ["TC-IF-004", "TC-IF-008"],
            },
            {
                "id": "REQ-IF-005",
                "summary": "Replace interest-rate calculations with profit-rate calculations for Islamic products",
                "description": (
                    "System shall replace interest-rate calculations with profit-rate calculations for all "
                    "Islamic products. No reference to 'interest' shall appear in Islamic product workflows.\n\n"
                    "**Acceptance Criteria:** Attempting to apply interest to an Islamic product throws a "
                    "ShariahViolationException."
                ),
                "test_cases": ["TC-IF-005"],
            },
            {
                "id": "REQ-IF-006",
                "summary": "Shariah Supervisory Board (SSB) approval workflow for new products",
                "description": (
                    "System shall support SSB approval workflow for new products — including submission, "
                    "review, approval/rejection, and audit trail.\n\n"
                    "**Acceptance Criteria:** A new Islamic product cannot be offered to customers until "
                    "an SSB member approves it through the workflow."
                ),
                "test_cases": ["TC-IF-007"],
            },
            {
                "id": "REQ-IF-007",
                "summary": "Wadiah (safekeeping) current accounts",
                "description": (
                    "System shall support Wadiah (safekeeping) current accounts. Funds are held in trust; "
                    "the bank may use them with implicit permission but guarantees return on demand.\n\n"
                    "**Acceptance Criteria:** A Wadiah current account can be opened with zero interest rate."
                ),
                "test_cases": ["TC-IF-006"],
            },
            {
                "id": "REQ-IF-008",
                "summary": "Wakala (agency) investment contracts",
                "description": (
                    "System shall support Wakala (agency) investment contracts. The customer appoints the "
                    "bank as agent (Wakeel) to invest funds for an agreed fee.\n\n"
                    "**Acceptance Criteria:** A Wakala investment contract is created with agreed management fee."
                ),
                "test_cases": [],
            },
            {
                "id": "REQ-IF-009",
                "summary": "Sukuk (Islamic bonds) in investment service",
                "description": (
                    "System shall support Sukuk (Islamic bonds) in the investment service. Sukuk represent "
                    "ownership in tangible assets or services.\n\n"
                    "**Acceptance Criteria:** Sukuk instruments can be listed, purchased, and tracked."
                ),
                "test_cases": [],
            },
            {
                "id": "REQ-IF-010",
                "summary": "Takaful (Islamic insurance) product references",
                "description": (
                    "System shall support Takaful (Islamic insurance) product references.\n\n"
                    "**Acceptance Criteria:** Takaful policy references can be stored with policy number, "
                    "provider, premium schedule, and claim status."
                ),
                "test_cases": [],
            },
        ],
    },
    {
        "key": "WS2",
        "title": "WS2 — Middle East Regulatory Compliance",
        "label": "ws2-regulatory-compliance",
        "priority": "Critical",
        "requirements": [
            {
                "id": "REQ-RC-001",
                "summary": "goAML suspicious transaction reports per CBUAE format",
                "description": (
                    "System shall generate goAML suspicious transaction reports per CBUAE format.\n\n"
                    "**Acceptance Criteria:** The system generates a goAML XML report that passes "
                    "CBUAE schema validation with all mandatory fields."
                ),
                "test_cases": ["TC-RC-004", "TC-RC-008"],
            },
            {
                "id": "REQ-RC-002",
                "summary": "CBUAE prudential returns",
                "description": (
                    "System shall generate CBUAE prudential returns including capital adequacy, "
                    "liquidity ratios, and large exposure reports.\n\n"
                    "**Acceptance Criteria:** Reports generated with correct ratios in CBUAE format."
                ),
                "test_cases": ["TC-RC-005", "TC-RC-008"],
            },
            {
                "id": "REQ-RC-003",
                "summary": "MENAFATF compliance frameworks",
                "description": (
                    "System shall support MENAFATF compliance frameworks including risk-based "
                    "approach assessments.\n\n"
                    "**Acceptance Criteria:** Risk categories and scoring align with MENAFATF criteria."
                ),
                "test_cases": [],
            },
            {
                "id": "REQ-RC-004",
                "summary": "UAE Federal Decree-Law No. 20/2018 AML/CFT reporting",
                "description": (
                    "System shall implement UAE Federal Decree-Law No. 20/2018 AML/CFT reporting.\n\n"
                    "**Acceptance Criteria:** All mandated reports can be generated."
                ),
                "test_cases": ["TC-RC-006"],
            },
            {
                "id": "REQ-RC-005",
                "summary": "Zakat at 2.5% on eligible assets with nisab threshold",
                "description": (
                    "System shall calculate Zakat at 2.5% on eligible assets with nisab threshold check.\n\n"
                    "**Acceptance Criteria:** If assets >= nisab, Zakat = assets * 2.5%. "
                    "If below nisab, Zakat = 0. Non-zakatable assets excluded."
                ),
                "test_cases": ["TC-RC-001", "TC-RC-002", "TC-RC-003"],
            },
            {
                "id": "REQ-RC-006",
                "summary": "5% GCC VAT on applicable banking fees",
                "description": (
                    "System shall handle 5% GCC VAT on applicable banking fees.\n\n"
                    "**Acceptance Criteria:** VAT-exempt transactions correctly identified. "
                    "VAT invoices generated per UAE FTA requirements."
                ),
                "test_cases": ["TC-RC-007"],
            },
            {
                "id": "REQ-RC-007",
                "summary": "SAMA reporting for KSA operations",
                "description": (
                    "System shall support SAMA reporting for KSA operations.\n\n"
                    "**Acceptance Criteria:** SAMA-format reports can be generated for KSA."
                ),
                "test_cases": [],
            },
            {
                "id": "REQ-RC-008",
                "summary": "AED 35,000 CTR threshold (not USD 10,000)",
                "description": (
                    "System shall use AED 35,000 CTR threshold for UAE operations.\n\n"
                    "**Acceptance Criteria:** CTR triggered at AED 35,000. Threshold configurable per jurisdiction."
                ),
                "test_cases": ["TC-RC-006"],
            },
        ],
    },
    {
        "key": "WS3",
        "title": "WS3 — KYC / Identity Verification",
        "label": "ws3-kyc-identity",
        "priority": "Critical",
        "requirements": [
            {
                "id": "REQ-KY-001",
                "summary": "Emirates ID format validation and ICP integration",
                "description": (
                    "System shall validate Emirates ID format (784-YYYY-NNNNNNN-C) and integrate "
                    "with ICP for real-time verification.\n\n"
                    "**Acceptance Criteria:** Valid Emirates ID passes; invalid formats, expired IDs, "
                    "and failed checksums are rejected with specific error codes."
                ),
                "test_cases": ["TC-KY-001", "TC-KY-002", "TC-KY-003", "TC-KY-008"],
            },
            {
                "id": "REQ-KY-002",
                "summary": "eKYC via UAE Pass (OAuth-based digital identity)",
                "description": (
                    "System shall support eKYC via UAE Pass.\n\n"
                    "**Acceptance Criteria:** Customer can authenticate via UAE Pass OAuth flow with "
                    "token exchange and identity binding."
                ),
                "test_cases": ["TC-KY-004", "TC-KY-005", "TC-KY-008"],
            },
            {
                "id": "REQ-KY-003",
                "summary": "Iqama (residency permit) for expat customers",
                "description": (
                    "System shall support Iqama for expat customers.\n\n"
                    "**Acceptance Criteria:** Iqama number and expiry validated and stored as primary "
                    "identity for non-citizen residents."
                ),
                "test_cases": ["TC-KY-006"],
            },
            {
                "id": "REQ-KY-004",
                "summary": "Trade License verification for business account onboarding",
                "description": (
                    "System shall verify Trade License for business accounts.\n\n"
                    "**Acceptance Criteria:** License number, issuing authority, activity codes, and "
                    "expiry date captured and validated."
                ),
                "test_cases": ["TC-KY-007"],
            },
            {
                "id": "REQ-KY-005",
                "summary": "UBO (Beneficial Ownership) registry integration per CBUAE",
                "description": (
                    "System shall integrate with UBO registry per CBUAE requirements.\n\n"
                    "**Acceptance Criteria:** UBOs with 25%+ ownership identified and screened."
                ),
                "test_cases": [],
            },
            {
                "id": "REQ-KY-006",
                "summary": "Saudi Absher National ID integration",
                "description": (
                    "System shall support Saudi Absher National ID integration.\n\n"
                    "**Acceptance Criteria:** Saudi customers can verify identity through Absher."
                ),
                "test_cases": [],
            },
        ],
    },
    {
        "key": "WS4",
        "title": "WS4 — Regional Payment Networks",
        "label": "ws4-payment-networks",
        "priority": "High",
        "requirements": [
            {
                "id": "REQ-PN-001",
                "summary": "UAEFTS (UAE Funds Transfer System) for RTGS",
                "description": (
                    "System shall integrate with UAEFTS for RTGS transfers in ISO 20022 format.\n\n"
                    "**Acceptance Criteria:** RTGS transfers submitted in ISO 20022 format with "
                    "proper acknowledgement handling."
                ),
                "test_cases": ["TC-PN-006", "TC-PN-008"],
            },
            {
                "id": "REQ-PN-002",
                "summary": "UAEIPS for real-time instant payments",
                "description": (
                    "System shall integrate with UAEIPS for instant payments.\n\n"
                    "**Acceptance Criteria:** Transfers settle within 10 seconds with IBAN validation."
                ),
                "test_cases": ["TC-PN-004", "TC-PN-005"],
            },
            {
                "id": "REQ-PN-003",
                "summary": "WPS (Wage Protection System) with SIF file generation",
                "description": (
                    "System shall support WPS with SIF file generation per MoL format.\n\n"
                    "**Acceptance Criteria:** SIF files generated with EDR header + SDR records. "
                    "Batch validation ensures all salaries can be funded."
                ),
                "test_cases": ["TC-PN-001", "TC-PN-002", "TC-PN-003"],
            },
            {
                "id": "REQ-PN-004",
                "summary": "SADAD for Saudi bill payment",
                "description": (
                    "System shall integrate with SADAD for Saudi bill payment.\n\n"
                    "**Acceptance Criteria:** Bill presentment, payment, and reconciliation supported."
                ),
                "test_cases": ["TC-PN-007"],
            },
            {
                "id": "REQ-PN-005",
                "summary": "SARIE for Saudi real-time payments",
                "description": (
                    "System shall integrate with SARIE for Saudi real-time payments.\n\n"
                    "**Acceptance Criteria:** Instant domestic transfers within KSA processed in real-time."
                ),
                "test_cases": [],
            },
            {
                "id": "REQ-PN-006",
                "summary": "GCC-RTGS and AFAQ cross-border payments",
                "description": (
                    "System shall support GCC-RTGS and AFAQ cross-border payment systems.\n\n"
                    "**Acceptance Criteria:** Inter-GCC settlements processed with proper routing."
                ),
                "test_cases": [],
            },
        ],
    },
    {
        "key": "WS5",
        "title": "WS5 — Multi-Currency & Remittance",
        "label": "ws5-multi-currency",
        "priority": "High",
        "requirements": [
            {
                "id": "REQ-MC-001",
                "summary": "Multi-currency wallets (AED, SAR, QAR, BHD, OMR, KWD)",
                "description": (
                    "System shall support multi-currency wallets for GCC currencies.\n\n"
                    "**Acceptance Criteria:** Customer can hold balances in 6 GCC currencies "
                    "with independent balance and transaction history."
                ),
                "test_cases": ["TC-MC-001"],
            },
            {
                "id": "REQ-MC-002",
                "summary": "CBUAE reference rates for real-time FX",
                "description": (
                    "System shall integrate with CBUAE reference rates for FX.\n\n"
                    "**Acceptance Criteria:** Rates refreshed at configurable intervals with spread."
                ),
                "test_cases": ["TC-MC-002", "TC-MC-003"],
            },
            {
                "id": "REQ-MC-003",
                "summary": "Dedicated remittance corridors (India, Pakistan, Philippines, Bangladesh)",
                "description": (
                    "System shall support dedicated remittance corridors.\n\n"
                    "**Acceptance Criteria:** Corridor-specific processing, compliance checks, "
                    "and beneficiary bank validation."
                ),
                "test_cases": ["TC-MC-006", "TC-MC-007"],
            },
            {
                "id": "REQ-MC-004",
                "summary": "Corridor-specific fees and AML limits per corridor",
                "description": (
                    "System shall calculate corridor-specific fees and apply AML limits.\n\n"
                    "**Acceptance Criteria:** Each corridor has defined fees and limits. "
                    "Excess transfers flagged for enhanced due diligence."
                ),
                "test_cases": ["TC-MC-004", "TC-MC-005", "TC-MC-007"],
            },
        ],
    },
    {
        "key": "WS6",
        "title": "WS6 — Arabic / RTL Frontend",
        "label": "ws6-arabic-rtl",
        "priority": "High",
        "requirements": [
            {
                "id": "REQ-AR-001",
                "summary": "Full RTL (right-to-left) layout for Arabic UI",
                "description": (
                    "System shall support full RTL layout for Arabic UI.\n\n"
                    "**Acceptance Criteria:** Entire UI renders RTL when Arabic locale is active. "
                    "No LTR artifacts remain."
                ),
                "test_cases": ["TC-AR-002"],
            },
            {
                "id": "REQ-AR-002",
                "summary": "Complete Arabic translations (rename es.json to ar.json)",
                "description": (
                    "System shall provide complete Arabic translations.\n\n"
                    "**Acceptance Criteria:** ar.json contains 100% of keys from en.json. "
                    "All values are Arabic strings."
                ),
                "test_cases": ["TC-AR-001"],
            },
            {
                "id": "REQ-AR-003",
                "summary": "Hijri calendar display alongside Gregorian",
                "description": (
                    "System shall support Hijri calendar display.\n\n"
                    "**Acceptance Criteria:** Date fields show both Hijri and Gregorian "
                    "when Arabic locale is active."
                ),
                "test_cases": ["TC-AR-003", "TC-AR-005"],
            },
            {
                "id": "REQ-AR-004",
                "summary": "Arabic-Indic numerals when Arabic locale is active",
                "description": (
                    "System shall format numbers using Arabic-Indic numerals.\n\n"
                    "**Acceptance Criteria:** Numeric values display using Arabic-Indic numerals "
                    "via Intl.NumberFormat('ar-AE')."
                ),
                "test_cases": ["TC-AR-004"],
            },
        ],
    },
]

# Test case definitions for sub-tasks
TEST_CASES = {
    "TC-IF-001": {"summary": "Murabaha markup calculation", "type": "unit", "preconditions": "IslamicFinanceCalculationService instantiated. Asset cost = 100,000 AED, markup = 15%.", "steps": "1. Call calculateMurabaha(100000, 15.0, 36)\n2. Verify total price = 115,000\n3. Verify monthly installment", "expected": "Total price = 115,000 AED. Monthly installment = 3,194.44 AED."},
    "TC-IF-002": {"summary": "Diminishing Musharaka equity reduction over time", "type": "unit", "preconditions": "Property = 1M AED, bank 80%, customer 20%, 20yr term.", "steps": "1. Call calculateMusharaka(1000000, 80.0, 240)\n2. Verify equity after 1, 120, 240 payments", "expected": "Equity reduces linearly. After 240 payments: customer 100%."},
    "TC-IF-003": {"summary": "Ijara rental schedule generation", "type": "unit", "preconditions": "Asset = 500K AED, 60 months, 6% rate.", "steps": "1. Call calculateIjara(500000, 6.0, 60)\n2. Verify 60 monthly payments\n3. Verify amount", "expected": "60 monthly payments of 2,500 AED."},
    "TC-IF-004": {"summary": "Mudarabah profit split by agreed ratio", "type": "unit", "preconditions": "Profit = 30K AED, ratio 60/40.", "steps": "1. Call calculateMudarabahProfit(30000, 60.0)\n2. Verify split", "expected": "Customer: 18K, Bank: 12K."},
    "TC-IF-005": {"summary": "Riba transaction rejection — ShariahViolationException", "type": "unit", "preconditions": "Islamic loan product exists.", "steps": "1. Apply COMPOUND_MONTHLY to Islamic product", "expected": "ShariahViolationException thrown."},
    "TC-IF-006": {"summary": "Wadiah account creation", "type": "unit", "preconditions": "KYC-verified customer.", "steps": "1. Create WADIAH_CURRENT account\n2. Verify properties", "expected": "Account created, interest=0, allows transactions."},
    "TC-IF-007": {"summary": "Islamic loan application E2E with SSB approval", "type": "integration", "preconditions": "KYC customer, Murabaha product, SSB reviewer.", "steps": "1. Submit Murabaha application\n2. Verify PENDING_SSB_APPROVAL\n3. SSB approves\n4. Verify APPROVED", "expected": "Loan approved through SSB workflow with audit trail."},
    "TC-IF-008": {"summary": "Profit distribution to Mudarabah savings account", "type": "integration", "preconditions": "Mudarabah account, balance 100K, profit 5K, ratio 70%.", "steps": "1. Trigger profit distribution\n2. Verify credit", "expected": "3,500 AED credited to customer."},
    "TC-RC-001": {"summary": "Zakat calculation above nisab — 2.5%", "type": "unit", "preconditions": "Eligible assets = 500K, nisab = 20K.", "steps": "1. Call calculateZakat(500000, 20000)", "expected": "Zakat = 12,500 AED."},
    "TC-RC-002": {"summary": "Zakat below nisab — returns zero", "type": "unit", "preconditions": "Assets = 15K, nisab = 20K.", "steps": "1. Call calculateZakat(15000, 20000)", "expected": "Zakat = 0."},
    "TC-RC-003": {"summary": "Zakat excludes non-zakatable assets", "type": "unit", "preconditions": "Total 600K, non-zakatable 400K, nisab 20K.", "steps": "1. Call calculateZakat with exclusions", "expected": "Eligible = 200K, Zakat = 5,000 AED."},
    "TC-RC-004": {"summary": "goAML report XML schema validation", "type": "unit", "preconditions": "Suspicious transaction with amount 50K AED.", "steps": "1. Generate goAML report\n2. Validate against schema", "expected": "XML passes CBUAE schema validation."},
    "TC-RC-005": {"summary": "CBUAE prudential return generation", "type": "unit", "preconditions": "Bank financial data available.", "steps": "1. Generate prudential return\n2. Verify ratios", "expected": "Correct capital adequacy ratio in CBUAE format."},
    "TC-RC-006": {"summary": "AML threshold uses AED 35,000 not USD 10,000", "type": "unit", "preconditions": "UAE jurisdiction configured.", "steps": "1. Process 34,999 AED — no CTR\n2. Process 35,000 AED — CTR triggered", "expected": "CTR at AED 35,000, not USD 10,000."},
    "TC-RC-007": {"summary": "VAT calculation on banking fees at 5%", "type": "unit", "preconditions": "Fee = 1,000 AED, VAT 5%.", "steps": "1. Calculate VAT non-exempt\n2. Calculate VAT exempt", "expected": "Non-exempt: 50 AED VAT. Exempt: 0 AED."},
    "TC-RC-008": {"summary": "End-to-end regulatory report submission", "type": "integration", "preconditions": "Suspicious transaction flagged, CBUAE endpoint mocked.", "steps": "1. Flag transaction\n2. Generate report\n3. Submit", "expected": "Report submitted with audit trail."},
    "TC-KY-001": {"summary": "Emirates ID valid format passes validation", "type": "unit", "preconditions": "EmiratesIdValidationService instantiated.", "steps": "1. Validate 784-1990-1234567-1", "expected": "Validation succeeds."},
    "TC-KY-002": {"summary": "Emirates ID expired — rejection", "type": "unit", "preconditions": "Expired Emirates ID.", "steps": "1. Validate with past expiry", "expected": "Rejected with EXPIRED reason."},
    "TC-KY-003": {"summary": "Emirates ID invalid checksum — rejection", "type": "unit", "preconditions": "Wrong check digit.", "steps": "1. Validate 784-1990-1234567-9", "expected": "Rejected with INVALID_CHECKSUM."},
    "TC-KY-004": {"summary": "UAE Pass OAuth — valid token returns identity", "type": "integration", "preconditions": "Mock OAuth endpoint, valid auth code.", "steps": "1. Exchange token\n2. Parse identity", "expected": "Identity with name, Emirates ID, nationality."},
    "TC-KY-005": {"summary": "UAE Pass expired token — throws exception", "type": "integration", "preconditions": "Mock endpoint, expired code.", "steps": "1. Exchange expired token", "expected": "Authentication exception thrown."},
    "TC-KY-006": {"summary": "Iqama document acceptance in KYC workflow", "type": "unit", "preconditions": "Non-citizen customer.", "steps": "1. Submit IQAMA document", "expected": "Document accepted and verified."},
    "TC-KY-007": {"summary": "Trade License verification for business accounts", "type": "integration", "preconditions": "Mock verification endpoint.", "steps": "1. Submit TRADE_LICENSE\n2. Verify", "expected": "License verified, details stored."},
    "TC-KY-008": {"summary": "Full KYC flow — Emirates ID + UAE Pass (E2E)", "type": "e2e", "preconditions": "Customer registered.", "steps": "1. Submit Emirates ID\n2. UAE Pass auth\n3. Verify KYC level", "expected": "KYC level = ENHANCED."},
    "TC-PN-001": {"summary": "WPS SIF file generation matches MoL format", "type": "unit", "preconditions": "Employer with 3 employees.", "steps": "1. Generate SIF\n2. Verify EDR + 3 SDR records", "expected": "SIF conforms to MoL format."},
    "TC-PN-002": {"summary": "WPS batch validation — all salaries validated", "type": "unit", "preconditions": "Sufficient funds, 5 records.", "steps": "1. Validate batch", "expected": "All 5 validated, total matches."},
    "TC-PN-003": {"summary": "WPS insufficient funds — batch rejected", "type": "unit", "preconditions": "Balance 10K, batch 50K.", "steps": "1. Process batch", "expected": "INSUFFICIENT_FUNDS rejection."},
    "TC-PN-004": {"summary": "UAEIPS instant transfer within SLA", "type": "integration", "preconditions": "Mock gateway, valid IBANs.", "steps": "1. Transfer 5K\n2. Measure time", "expected": "Settled within 10s."},
    "TC-PN-005": {"summary": "UAEIPS IBAN format validation", "type": "unit", "preconditions": "IbanValidator instantiated.", "steps": "1. Validate UAE IBAN\n2. Validate invalid\n3. Validate KSA", "expected": "Valid pass, invalid rejected."},
    "TC-PN-006": {"summary": "UAEFTS RTGS message format", "type": "unit", "preconditions": "Transfer 1M AED.", "steps": "1. Generate message\n2. Verify ISO 20022", "expected": "pacs.008 format with all fields."},
    "TC-PN-007": {"summary": "SADAD bill presentment and payment", "type": "integration", "preconditions": "Mock SADAD, outstanding bill.", "steps": "1. Retrieve bill\n2. Pay\n3. Verify reconciliation", "expected": "Payment processed, reconciliation recorded."},
    "TC-PN-008": {"summary": "End-to-end domestic transfer via UAEFTS (E2E)", "type": "e2e", "preconditions": "Funded account, valid receiver, mock UAEFTS.", "steps": "1. Transfer 500K\n2. Simulate settlement", "expected": "Account debited, status SETTLED."},
    "TC-MC-001": {"summary": "Multi-currency wallet creation for AED/SAR/QAR/BHD/OMR/KWD", "type": "unit", "preconditions": "Verified customer.", "steps": "1. Create 6 wallets\n2. Verify zero balances", "expected": "6 wallets created, linked to parent."},
    "TC-MC-002": {"summary": "Currency conversion with spread", "type": "unit", "preconditions": "AED/SAR mid-rate 1.02, spread 0.5%.", "steps": "1. Convert 10K AED to SAR", "expected": "10,149 SAR after spread."},
    "TC-MC-003": {"summary": "CBUAE reference rate lookup", "type": "integration", "preconditions": "Mock CBUAE endpoint.", "steps": "1. Request AED/USD rate\n2. Verify cache", "expected": "Rate with source CBUAE, cached 15min."},
    "TC-MC-004": {"summary": "India corridor remittance fee calculation", "type": "unit", "preconditions": "India corridor: flat 15 AED, 0.25%, min 15.", "steps": "1. Calculate fee for 5K AED", "expected": "Fee = 15 AED (min applies)."},
    "TC-MC-005": {"summary": "Remittance AML limit enforcement", "type": "unit", "preconditions": "Limit 100K/month, already sent 95K.", "steps": "1. Attempt 10K remittance", "expected": "Rejected — exceeds monthly limit."},
    "TC-MC-006": {"summary": "Beneficiary bank validation", "type": "unit", "preconditions": "SWIFT codes available.", "steps": "1. Validate SBININBB\n2. Validate invalid", "expected": "Valid resolves bank name, invalid rejected."},
    "TC-MC-007": {"summary": "End-to-end remittance to India corridor (E2E)", "type": "e2e", "preconditions": "KYC customer, funded wallet, India corridor.", "steps": "1. Submit 20K AED to India\n2. Verify AML, FX, fees", "expected": "Remittance processed end-to-end."},
    "TC-AR-001": {"summary": "i18n key completeness — ar.json has all keys from en.json", "type": "unit", "preconditions": "en.json and ar.json exist.", "steps": "1. Compare key sets", "expected": "100% key coverage."},
    "TC-AR-002": {"summary": "RTL layout renders correctly with dir='rtl'", "type": "e2e", "preconditions": "App running, Arabic locale.", "steps": "1. Switch to Arabic\n2. Verify dir=rtl", "expected": "Full RTL layout, no LTR artifacts."},
    "TC-AR-003": {"summary": "Hijri calendar conversion accuracy", "type": "unit", "preconditions": "HijriDateDisplay available.", "steps": "1. Convert 2026-03-20\n2. Verify Hijri date", "expected": "Correct Hijri date within 1-day tolerance."},
    "TC-AR-004": {"summary": "Arabic number formatting via Intl.NumberFormat('ar-AE')", "type": "unit", "preconditions": "arabicFormat utility available.", "steps": "1. Format 12345.67", "expected": "Output uses Arabic-Indic numerals."},
    "TC-AR-005": {"summary": "Date picker works in Hijri mode", "type": "e2e", "preconditions": "Arabic locale, date picker rendered.", "steps": "1. Open picker\n2. Select date", "expected": "Hijri months shown, both values captured."},
}


def build_jira_payload():
    """Build the full JIRA import payload structure."""
    payload = {"epics": []}

    for ws in WORKSTREAMS:
        epic = {
            "issue_type": "Epic",
            "project": PROJECT_KEY,
            "summary": ws["title"],
            "description": f"Epic for {ws['title']}. Priority: {ws['priority']}.",
            "labels": [ws["label"], "middle-east-compliance"],
            "priority": ws["priority"],
            "stories": [],
        }

        for req in ws["requirements"]:
            tc_links = ", ".join(req["test_cases"]) if req["test_cases"] else "None yet"
            story = {
                "issue_type": "Story",
                "project": PROJECT_KEY,
                "summary": f"[{req['id']}] {req['summary']}",
                "description": f"{req['description']}\n\n**Linked Test Cases:** {tc_links}",
                "labels": [ws["label"], "middle-east-compliance"],
                "priority": ws["priority"],
                "subtasks": [],
            }

            for tc_id in req["test_cases"]:
                tc = TEST_CASES.get(tc_id)
                if tc:
                    subtask = {
                        "issue_type": "Sub-task",
                        "project": PROJECT_KEY,
                        "summary": f"[{tc_id}] {tc['summary']}",
                        "description": (
                            f"**Type:** {tc['type']}\n\n"
                            f"**Preconditions:** {tc['preconditions']}\n\n"
                            f"**Steps:**\n{tc['steps']}\n\n"
                            f"**Expected Result:** {tc['expected']}"
                        ),
                        "labels": [ws["label"], f"test-{tc['type']}"],
                    }
                    story["subtasks"].append(subtask)

            epic["stories"].append(story)

        payload["epics"].append(epic)

    return payload


def create_jira_issues(jira_url, jira_user, jira_token, payload):
    """Create issues in JIRA using the REST API."""
    if not HAS_REQUESTS:
        print("ERROR: 'requests' library not installed. Run: pip install requests")
        return False

    auth = (jira_user, jira_token)
    headers = {"Content-Type": "application/json", "Accept": "application/json"}
    base = jira_url.rstrip("/")
    api = f"{base}/rest/api/2/issue"

    created = {"epics": 0, "stories": 0, "subtasks": 0}

    for epic_data in payload["epics"]:
        # Create Epic
        epic_body = {
            "fields": {
                "project": {"key": PROJECT_KEY},
                "summary": epic_data["summary"],
                "description": epic_data["description"],
                "issuetype": {"name": "Epic"},
                "labels": epic_data["labels"],
                "customfield_10011": epic_data["summary"],  # Epic Name field
            }
        }
        resp = requests.post(api, json=epic_body, auth=auth, headers=headers)
        if resp.status_code not in (200, 201):
            print(f"WARN: Failed to create epic '{epic_data['summary']}': {resp.status_code} {resp.text}")
            continue
        epic_key = resp.json()["key"]
        created["epics"] += 1
        print(f"  Created Epic: {epic_key} — {epic_data['summary']}")

        for story_data in epic_data["stories"]:
            # Create Story under Epic
            story_body = {
                "fields": {
                    "project": {"key": PROJECT_KEY},
                    "summary": story_data["summary"],
                    "description": story_data["description"],
                    "issuetype": {"name": "Story"},
                    "labels": story_data["labels"],
                    "customfield_10014": epic_key,  # Epic Link
                }
            }
            resp = requests.post(api, json=story_body, auth=auth, headers=headers)
            if resp.status_code not in (200, 201):
                print(f"  WARN: Failed to create story '{story_data['summary']}': {resp.status_code}")
                continue
            story_key = resp.json()["key"]
            created["stories"] += 1
            print(f"    Created Story: {story_key} — {story_data['summary']}")

            for subtask_data in story_data["subtasks"]:
                # Create Sub-task under Story
                subtask_body = {
                    "fields": {
                        "project": {"key": PROJECT_KEY},
                        "parent": {"key": story_key},
                        "summary": subtask_data["summary"],
                        "description": subtask_data["description"],
                        "issuetype": {"name": "Sub-task"},
                        "labels": subtask_data["labels"],
                    }
                }
                resp = requests.post(api, json=subtask_body, auth=auth, headers=headers)
                if resp.status_code not in (200, 201):
                    print(f"    WARN: Failed to create subtask '{subtask_data['summary']}': {resp.status_code}")
                    continue
                st_key = resp.json()["key"]
                created["subtasks"] += 1
                print(f"      Created Sub-task: {st_key} — {subtask_data['summary']}")

    # Attach HTML documents to first epic
    if created["epics"] > 0:
        docs_dir = Path(__file__).resolve().parent.parent / "docs"
        for doc_name in ["middle-east-urs.html", "middle-east-test-spec.html"]:
            doc_path = docs_dir / doc_name
            if doc_path.exists():
                attach_url = f"{base}/rest/api/2/issue/{epic_key}/attachments"
                attach_headers = {"X-Atlassian-Token": "no-check"}
                with open(doc_path, "rb") as f:
                    resp = requests.post(
                        attach_url,
                        auth=auth,
                        headers=attach_headers,
                        files={"file": (doc_name, f, "text/html")},
                    )
                if resp.status_code in (200, 201):
                    print(f"  Attached {doc_name} to {epic_key}")
                else:
                    print(f"  WARN: Failed to attach {doc_name}: {resp.status_code}")

    print(f"\nSummary: {created['epics']} Epics, {created['stories']} Stories, {created['subtasks']} Sub-tasks created.")
    return True


def generate_csv(payload, output_path):
    """Generate a JIRA-importable CSV file."""
    import csv

    rows = []
    for epic in payload["epics"]:
        rows.append({
            "Issue Type": "Epic",
            "Summary": epic["summary"],
            "Description": epic["description"],
            "Labels": ";".join(epic["labels"]),
            "Priority": epic["priority"],
            "Epic Name": epic["summary"],
            "Epic Link": "",
            "Parent": "",
        })
        for story in epic["stories"]:
            rows.append({
                "Issue Type": "Story",
                "Summary": story["summary"],
                "Description": story["description"],
                "Labels": ";".join(story["labels"]),
                "Priority": story["priority"],
                "Epic Name": "",
                "Epic Link": epic["summary"],
                "Parent": "",
            })
            for subtask in story["subtasks"]:
                rows.append({
                    "Issue Type": "Sub-task",
                    "Summary": subtask["summary"],
                    "Description": subtask["description"],
                    "Labels": ";".join(subtask["labels"]),
                    "Priority": "",
                    "Epic Name": "",
                    "Epic Link": "",
                    "Parent": story["summary"],
                })

    fieldnames = ["Issue Type", "Summary", "Description", "Labels", "Priority", "Epic Name", "Epic Link", "Parent"]
    with open(output_path, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)

    print(f"CSV written to {output_path} ({len(rows)} rows)")


def main():
    jira_url = os.environ.get("JIRA_URL", "")
    jira_user = os.environ.get("JIRA_USER", "")
    jira_token = os.environ.get("JIRA_TOKEN", "")

    payload = build_jira_payload()

    # Always write JSON payload
    script_dir = Path(__file__).resolve().parent
    json_path = script_dir / "jira-import-payload.json"
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(payload, f, indent=2, ensure_ascii=False)
    print(f"JSON payload written to {json_path}")

    # Also write CSV
    csv_path = script_dir / "jira-import-payload.csv"
    generate_csv(payload, csv_path)

    if jira_url and jira_user and jira_token:
        print(f"\nConnecting to JIRA at {jira_url} ...")
        success = create_jira_issues(jira_url, jira_user, jira_token, payload)
        if not success:
            print("JIRA import failed. Use the JSON/CSV files for manual import.")
            sys.exit(1)
    else:
        print("\nJIRA credentials not set (JIRA_URL, JIRA_USER, JIRA_TOKEN).")
        print("To import manually:")
        print(f"  1. Use JIRA CSV import with: {csv_path}")
        print(f"  2. Or use the REST API payload: {json_path}")
        print("\nTo run with credentials:")
        print("  export JIRA_URL=https://your-instance.atlassian.net")
        print("  export JIRA_USER=your-email@example.com")
        print("  export JIRA_TOKEN=your-api-token")
        print("  python3 scripts/jira_import.py")


if __name__ == "__main__":
    main()
