package com.smartparking.service;

import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Payment Simulation Service
 * Handles payment processing for parking fees without real money transactions.
 * Simulates success/failure and generates transaction IDs on success.
 */
@Service
public class PaymentService {

    private static final double SUCCESS_PROBABILITY = 0.95; // 95% success rate for simulation

    /**
     * Process payment simulation
     * @param amount Parking fee amount
     * @return PaymentResult containing success status and transaction ID
     */
    public PaymentResult processPayment(Double amount) {
        if (amount == null || amount <= 0) {
            return new PaymentResult(false, null, "Invalid amount");
        }

        // Simulate payment processing (random success/failure)
        boolean isSuccess = Math.random() < SUCCESS_PROBABILITY;

        if (isSuccess) {
            // Generate unique transaction ID on success
            String transactionId = generateTransactionId();
            return new PaymentResult(true, transactionId, "Payment successful");
        } else {
            return new PaymentResult(false, null, "Payment failed - Please try again");
        }
    }

    /**
     * Generate unique transaction ID using UUID
     */
    private String generateTransactionId() {
        return "TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    /**
     * Payment Result DTO
     */
    public static class PaymentResult {
        private final boolean success;
        private final String transactionId;
        private final String message;

        public PaymentResult(boolean success, String transactionId, String message) {
            this.success = success;
            this.transactionId = transactionId;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public String getMessage() {
            return message;
        }
    }
}
