package com.BBC_Ops.BBC_Ops.Controller;

import com.BBC_Ops.BBC_Ops.Model.Wallet;
import com.BBC_Ops.BBC_Ops.Service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("wallets")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping("/{customerId}")
    public ResponseEntity<Wallet> getWalletByCustomerId(@PathVariable Long customerId) {
        Wallet wallet = walletService.findByCustomerId(customerId);
        if (wallet != null) {
            return ResponseEntity.ok(wallet);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/add-money")
    public ResponseEntity<Map<String, String>> addMoneyToWallet(@RequestBody Map<String, Object> request) {
        Long customerId = ((Number) request.get("customerId")).longValue();
        double amount = ((Number) request.get("amount")).doubleValue();
        String paymentMethod = (String) request.get("paymentMethod");

        boolean success = walletService.addMoneyToWallet(customerId, amount, paymentMethod);

        Map<String, String> response = new HashMap<>();
        if (success) {
            response.put("message", "Money added successfully!");
            return ResponseEntity.ok(response);

        } else {
            response.put("message", "Invalid payment method or customer not found.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    static class AddMoneyRequest {
        private Long customerId;
        private double amount;
        private String paymentMethod;

        public Long getCustomerId() {
            return customerId;
        }

        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }
    }
}
