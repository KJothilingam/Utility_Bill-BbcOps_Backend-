package com.BBC_Ops.BBC_Ops.Model;

import com.BBC_Ops.BBC_Ops.Enum.PaymentMethod;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "payment_record")
public class PaymentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String invoiceId;
    private String meterNumber;
    private double unitConsumed;
    private Date dueDate;
    private double totalBillAmount;
    private double amountPaid;
    private double discountApplied;
    private double finalAmountPaid;
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    private Date paymentDate;
    private String billingMonth;
    private String transactionId;

    //  Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getBillingMonth() {
        return billingMonth;
    }

    public void setBillingMonth(String billingMonth) {
        this.billingMonth = billingMonth;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public String toString() {
        return "PaymentRecord{" +
                "id=" + id +
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
                ", billingMonth='" + billingMonth + '\'' +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }
}