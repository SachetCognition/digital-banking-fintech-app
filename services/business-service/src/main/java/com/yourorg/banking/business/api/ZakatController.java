package com.yourorg.banking.business.api;

import com.yourorg.banking.business.service.ZakatCalculationService;
import com.yourorg.banking.business.service.ZakatCalculationService.ZakatAssets;
import com.yourorg.banking.business.service.ZakatCalculationService.ZakatAssessment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/zakat")
public class ZakatController {

    @Autowired
    private ZakatCalculationService zakatService;

    /**
     * POST /api/v1/zakat/calculate
     * Body: { customerId, assets: {...}, goldPricePerGram, silverPricePerGram }
     */
    @PostMapping("/calculate")
    public ResponseEntity<ZakatAssessment> calculate(@RequestBody ZakatRequest request) {
        ZakatAssessment assessment = zakatService.calculateZakat(
            request.getCustomerId(),
            request.getAssets(),
            request.getGoldPricePerGram(),
            request.getSilverPricePerGram());
        return ResponseEntity.ok(assessment);
    }

    /**
     * GET /api/v1/zakat/history/{customerId}
     */
    @GetMapping("/history/{customerId}")
    public ResponseEntity<List<ZakatAssessment>> history(@PathVariable String customerId) {
        return ResponseEntity.ok(zakatService.getAssessmentHistory(customerId));
    }

    /**
     * GET /api/v1/zakat/hawl/{customerId}?nisab=...
     */
    @GetMapping("/hawl/{customerId}")
    public ResponseEntity<Map<String, Object>> hawlCheck(
            @PathVariable String customerId,
            @RequestParam BigDecimal nisab) {
        boolean hawlMet = zakatService.verifyHawl(customerId, nisab);
        return ResponseEntity.ok(Map.of(
            "customerId", customerId,
            "hawlVerified", hawlMet,
            "lunarYearDays", 354));
    }

    public static class ZakatRequest {
        private String customerId;
        private ZakatAssets assets;
        private BigDecimal goldPricePerGram;
        private BigDecimal silverPricePerGram;

        public String getCustomerId() { return customerId; }
        public void setCustomerId(String v) { this.customerId = v; }
        public ZakatAssets getAssets() { return assets; }
        public void setAssets(ZakatAssets v) { this.assets = v; }
        public BigDecimal getGoldPricePerGram() { return goldPricePerGram; }
        public void setGoldPricePerGram(BigDecimal v) { this.goldPricePerGram = v; }
        public BigDecimal getSilverPricePerGram() { return silverPricePerGram; }
        public void setSilverPricePerGram(BigDecimal v) { this.silverPricePerGram = v; }
    }
}
