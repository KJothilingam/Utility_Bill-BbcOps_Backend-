package com.BBC_Ops.BBC_Ops.Service.BillingStrategy;

import com.BBC_Ops.BBC_Ops.Model.BillingStrategy;
import com.BBC_Ops.BBC_Ops.Model.ConnectionType;
import org.springframework.stereotype.Service;

@Service
public class BillingContext {

    private BillingStrategy billingStrategy;

    public void setBillingStrategy(BillingStrategy billingStrategy) {
        this.billingStrategy = billingStrategy;
    }

    public double generateBill(int unitConsumption, ConnectionType connectionType) {
        return billingStrategy.calculateBill(unitConsumption, connectionType);
    }
}
