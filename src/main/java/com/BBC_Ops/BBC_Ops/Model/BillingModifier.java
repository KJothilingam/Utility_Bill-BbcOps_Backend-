package com.BBC_Ops.BBC_Ops.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "billing_modifiers")
public class BillingModifier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String modifierName; // e.g., "DISCOUNT", "PEAK_HOUR_SURCHARGE"

    @Column(nullable = false)
    private double value;

    // Getters and Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModifierName() {
        return modifierName;
    }

    public void setModifierName(String modifierName) {
        this.modifierName = modifierName;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
