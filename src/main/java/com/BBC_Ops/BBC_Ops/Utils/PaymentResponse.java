package com.BBC_Ops.BBC_Ops.Utils;

import com.BBC_Ops.BBC_Ops.Enum.PaymentMethod;
import java.util.Date;

public class PaymentResponse {
    private boolean success;
    private String message;
    private String invoiceId;
    private String meterNumber;
    private double unitConsumed;
    private Date dueDate;
    private double totalBillAmount;
    private double amountPaid;
    private double discountApplied;
    private double finalAmountPaid;
    private PaymentMethod paymentMethod;
    private Date paymentDate;
    private String billingMonth;  // ✅ Ensure it's a String
    private String transactionId; // ✅ Ensure it's a String

    public PaymentResponse(boolean success, String message, String invoiceId, String meterNumber, double unitConsumed,
                           Date dueDate, double totalBillAmount, double amountPaid, double discountApplied,
                           double finalAmountPaid, PaymentMethod paymentMethod, Date paymentDate,
                           String billingMonth, String transactionId) {  // ✅ Use String for transactionId
        this.success = success;
        this.message = message;
        this.invoiceId = invoiceId;
        this.meterNumber = meterNumber;
        this.unitConsumed = unitConsumed;
        this.dueDate = dueDate;
        this.totalBillAmount = totalBillAmount;
        this.amountPaid = amountPaid;
        this.discountApplied = discountApplied;
        this.finalAmountPaid = finalAmountPaid;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
        this.billingMonth = billingMonth;
        this.transactionId = transactionId;
    }

    // ✅ Update Getters and Setters
    public String getBillingMonth() { return billingMonth; }
    public void setBillingMonth(String billingMonth) { this.billingMonth = billingMonth; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}
