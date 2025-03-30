package com.BBC_Ops.BBC_Ops.Service;

import com.BBC_Ops.BBC_Ops.Model.BillingStrategy;
import org.springframework.stereotype.Service;

@Service
public class PeakHourBillingStrategy implements BillingStrategy {
    private static final double STANDARD_RATE = 41.50;
    private static final double PEAK_HOUR_SURCHARGE = 0.10; // 10% extra charge

    @Override
    public double calculateBill(int unitConsumption) {
        double total = unitConsumption * STANDARD_RATE;
        return total + (total * PEAK_HOUR_SURCHARGE);
    }
}
