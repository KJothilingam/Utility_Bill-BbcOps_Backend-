package com.BBC_Ops.BBC_Ops.Utils;


import java.util.List;

public class MonthlyPaymentDTO {
    private List<String> months;
    private List<Double> amounts;

    public MonthlyPaymentDTO(List<String> months, List<Double> amounts) {
        this.months = months;
        this.amounts = amounts;
    }

    public List<String> getMonths() { return months; }

    public List<Double> getAmounts() { return amounts; }
}