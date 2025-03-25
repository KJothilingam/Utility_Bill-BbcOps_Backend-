package com.BBC_Ops.BBC_Ops.Model;

import com.BBC_Ops.BBC_Ops.Enum.PaymentStatus;
import jakarta.persistence.*;
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

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date monthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(nullable = false)
    private Double totalBillAmount;

    @Column(nullable = false)
    private Double discountApplied = 0.00;

    @Column(nullable = false, unique = true)
    private String invoiceId;
}