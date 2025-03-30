package com.BBC_Ops.BBC_Ops.Model;

import com.BBC_Ops.BBC_Ops.Enum.PaymentStatus;
import jakarta.persistence.*;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "bills")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long billId;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, unique = true)
    private String invoiceId;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date monthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(nullable = false)
    private Double totalBillAmount;

    @Column(nullable = false)
    private Double discountApplied = 0.00;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdAt = new Date();

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date dueDate;

    @OneToOne(mappedBy = "bill", cascade = CascadeType.ALL)
    private Transaction transaction;

    public Bill() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(this.createdAt);
        calendar.add(Calendar.DAY_OF_MONTH, 10);
        this.dueDate = calendar.getTime();
    }

    @Override
    public String toString() {
        return "Bill{" +
                "billId=" + billId +
                ", customer=" + customer +
                ", invoiceId='" + invoiceId + '\'' +
                ", monthDate=" + monthDate +
                ", paymentStatus=" + paymentStatus +
                ", totalBillAmount=" + totalBillAmount +
                ", discountApplied=" + discountApplied +
                ", createdAt=" + createdAt +
                ", dueDate=" + dueDate +
                ", transaction=" + transaction +
                '}';
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Date getMonthDate() {
        return monthDate;
    }

    public void setMonthDate(Date monthDate) {
        this.monthDate = monthDate;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Double getTotalBillAmount() {
        return totalBillAmount;
    }

    public void setTotalBillAmount(Double totalBillAmount) {
        this.totalBillAmount = totalBillAmount;
    }

    public Double getDiscountApplied() {
        return discountApplied;
    }

    public void setDiscountApplied(Double discountApplied) {
        this.discountApplied = discountApplied;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}
