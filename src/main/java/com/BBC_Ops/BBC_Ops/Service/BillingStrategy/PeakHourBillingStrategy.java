package com.BBC_Ops.BBC_Ops.Service.BillingStrategy;

import com.BBC_Ops.BBC_Ops.Model.BillingStrategy;
import com.BBC_Ops.BBC_Ops.Model.ConnectionType;
import com.BBC_Ops.BBC_Ops.Repository.BillingModifierRepository;
import com.BBC_Ops.BBC_Ops.Repository.BillingRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PeakHourBillingStrategy implements BillingStrategy {

    @Autowired
    private BillingRateRepository billingRateRepository;

    @Autowired
    private BillingModifierRepository billingModifierRepository;

    @Override
    public double calculateBill(int unitConsumption, ConnectionType connectionType) {
        double rate = billingRateRepository.findByConnectionType(connectionType)
                .orElseThrow(() -> new RuntimeException("Rate not found"))
                .getRate();

        double surcharge = billingModifierRepository.findByModifierName("PEAK_HOUR_SURCHARGE")
                .orElseThrow(() -> new RuntimeException("PEAK_HOUR_SURCHARGE not configured"))
                .getValue();

        double total = unitConsumption * rate;
        return total + (total * surcharge);
    }
}
