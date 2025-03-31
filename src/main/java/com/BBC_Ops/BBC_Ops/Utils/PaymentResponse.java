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

    public PaymentResponse(boolean success, String message, String invoiceId, String meterNumber, double unitConsumed,
                           Date dueDate, double totalBillAmount, double amountPaid, double discountApplied,
                           double finalAmountPaid, PaymentMethod paymentMethod, Date paymentDate) {
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
    }

    // Getters and Setters


    @Override
    public String toString() {
        return "PaymentResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", invoiceId='" + invoiceId + '\'' +
                ", meterNumber='" + meterNumber + '\'' +
                ", unitConsumed=" + unitConsumed +
                ", dueDate=" + dueDate +
                ", totalBillAmount=" + totalBillAmount +
                ", amountPaid=" + amountPaid +
                ", discountApplied=" + discountApplied +
                ", finalAmountPaid=" + finalAmountPaid +
                ", paymentMethod=" + paymentMethod +
                ", paymentDate=" + paymentDate +
                '}';
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getMeterNumber() {
        return meterNumber;
    }

    public void setMeterNumber(String meterNumber) {
        this.meterNumber = meterNumber;
    }

    public double getUnitConsumed() {
        return unitConsumed;
    }

    public void setUnitConsumed(double unitConsumed) {
        this.unitConsumed = unitConsumed;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public double getTotalBillAmount() {
        return totalBillAmount;
    }

    public void setTotalBillAmount(double totalBillAmount) {
        this.totalBillAmount = totalBillAmount;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public double getDiscountApplied() {
        return discountApplied;
    }

    public void setDiscountApplied(double discountApplied) {
        this.discountApplied = discountApplied;
    }

    public double getFinalAmountPaid() {
        return finalAmountPaid;
    }

    public void setFinalAmountPaid(double finalAmountPaid) {
        this.finalAmountPaid = finalAmountPaid;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }
}
