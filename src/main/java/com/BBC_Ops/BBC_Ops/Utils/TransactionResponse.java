package com.BBC_Ops.BBC_Ops.Utils;

import com.BBC_Ops.BBC_Ops.Model.Transaction;

public class TransactionResponse {
    private Transaction transaction;
    private double discountApplied;
    private String receiptUrl;

    public TransactionResponse(Transaction transaction, double discountApplied, String receiptUrl) {
        this.transaction = transaction;
        this.discountApplied = discountApplied;
        this.receiptUrl = receiptUrl;
    }

    public Transaction getTransaction() { return transaction; }
    public double getDiscountApplied() { return discountApplied; }
    public String getReceiptUrl() { return receiptUrl; }
}
