package com.yourorg.banking.payments.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class UaeftsService {

    public String generateRtgsMessage(String senderBic, String receiverBic,
                                       BigDecimal amount, String currency, String valueDate) {
        if (senderBic == null || senderBic.isBlank()) {
            throw new IllegalArgumentException("Sender BIC must not be null or blank");
        }
        if (receiverBic == null || receiverBic.isBlank()) {
            throw new IllegalArgumentException("Receiver BIC must not be null or blank");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency must not be null or blank");
        }
        if (valueDate == null || valueDate.isBlank()) {
            throw new IllegalArgumentException("Value date must not be null or blank");
        }

        String msgId = "UAEFTS" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String creationDateTime = LocalDate.now().toString();

        return String.format(
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                  <FIToFICstmrCdtTrf>
                    <GrpHdr>
                      <MsgId>%s</MsgId>
                      <CreDtTm>%s</CreDtTm>
                      <NbOfTxs>1</NbOfTxs>
                      <SttlmInf>
                        <SttlmMtd>CLRG</SttlmMtd>
                      </SttlmInf>
                    </GrpHdr>
                    <CdtTrfTxInf>
                      <PmtId>
                        <EndToEndId>%s</EndToEndId>
                      </PmtId>
                      <IntrBkSttlmAmt Ccy="%s">%s</IntrBkSttlmAmt>
                      <IntrBkSttlmDt>%s</IntrBkSttlmDt>
                      <InstgAgt>
                        <FinInstnId>
                          <BICFI>%s</BICFI>
                        </FinInstnId>
                      </InstgAgt>
                      <InstdAgt>
                        <FinInstnId>
                          <BICFI>%s</BICFI>
                        </FinInstnId>
                      </InstdAgt>
                    </CdtTrfTxInf>
                  </FIToFICstmrCdtTrf>
                </Document>""",
                msgId, creationDateTime, msgId, currency, amount.toPlainString(),
                valueDate, senderBic, receiverBic);
    }

    public TransferResult submitTransfer(String senderBic, String receiverBic,
                                          BigDecimal amount, String currency, String valueDate) {
        try {
            String rtgsMessage = generateRtgsMessage(senderBic, receiverBic, amount, currency, valueDate);
            UUID transactionId = UUID.randomUUID();
            return new TransferResult(true, "RTGS transfer submitted successfully",
                    transactionId, "ACCEPTED", rtgsMessage);
        } catch (IllegalArgumentException e) {
            return new TransferResult(false, "Transfer failed: " + e.getMessage(),
                    null, "REJECTED", null);
        }
    }

    public record TransferResult(
            boolean success,
            String message,
            UUID transactionId,
            String status,
            String rtgsMessage
    ) {
    }
}
