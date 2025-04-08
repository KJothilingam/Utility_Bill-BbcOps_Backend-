package com.BBC_Ops.BBC_Ops.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "customerId")
    private Customer customer;

    @Column(nullable = false)
    private double creditCardBalance;

    @Column(nullable = false)
    private double debitCardBalance;

    @Column(nullable = false)
    private double walletBalance;

    @Column(nullable = false)
    private double upiBalance;

    // Constructors
    public Wallet() {}

    public Wallet(Customer customer, double creditCardBalance, double debitCardBalance, double walletBalance, double upiBalance) {
        this.customer = customer;
        this.creditCardBalance = creditCardBalance;
        this.debitCardBalance = debitCardBalance;
        this.walletBalance = walletBalance;
        this.upiBalance = upiBalance;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public double getCreditCardBalance() {
        return creditCardBalance;
    }

    public void setCreditCardBalance(double creditCardBalance) {
        this.creditCardBalance = creditCardBalance;
    }

    public double getDebitCardBalance() {
        return debitCardBalance;
    }

    public void setDebitCardBalance(double debitCardBalance) {
        this.debitCardBalance = debitCardBalance;
    }

    public double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(double walletBalance) {
        this.walletBalance = walletBalance;
    }

    public double getUpiBalance() {
        return upiBalance;
    }

    public void setUpiBalance(double upiBalance) {
        this.upiBalance = upiBalance;
    }
}
