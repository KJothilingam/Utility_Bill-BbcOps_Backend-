package com.BBC_Ops.BBC_Ops.Service.BillingStrategy;

import com.BBC_Ops.BBC_Ops.Model.BillingStrategy;
import org.springframework.stereotype.Service;

@Service
public class StandardBillingStrategy implements BillingStrategy {

    private static final double STANDARD_RATE = 41.50;

    @Override
    public double calculateBill(int unitConsumption) {
        return unitConsumption * STANDARD_RATE;
    }

}
