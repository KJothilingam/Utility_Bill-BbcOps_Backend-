package com.BBC_Ops.BBC_Ops.Service;

import com.BBC_Ops.BBC_Ops.Model.BillingStrategy;
import org.springframework.stereotype.Service;

@Service
public class BillingContext {
    private BillingStrategy billingStrategy;

    public void setBillingStrategy(BillingStrategy billingStrategy) {
        this.billingStrategy = billingStrategy;
    }

    public double generateBill(int unitConsumption) {
        return billingStrategy.calculateBill(unitConsumption);
    }
}
