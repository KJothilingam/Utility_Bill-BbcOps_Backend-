package com.BBC_Ops.BBC_Ops.Service;

import com.BBC_Ops.BBC_Ops.Model.BillingStrategy;
import org.springframework.stereotype.Service;

@Service
public class DiscountedBillingStrategy implements BillingStrategy {
    private static final double STANDARD_RATE = 41.50;
    private static final double DISCOUNT = 0.05; // 5% discount


    @Override
    public double calculateBill(int unitConsumption) {
        double total = unitConsumption * STANDARD_RATE;
        return total - (total * DISCOUNT);
    }
}
