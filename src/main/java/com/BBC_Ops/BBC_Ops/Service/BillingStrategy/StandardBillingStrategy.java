package com.BBC_Ops.BBC_Ops.Service.BillingStrategy;

import com.BBC_Ops.BBC_Ops.Model.BillingStrategy;
import com.BBC_Ops.BBC_Ops.Model.ConnectionType;
import com.BBC_Ops.BBC_Ops.Repository.BillingRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StandardBillingStrategy implements BillingStrategy {

    @Autowired
    private BillingRateRepository billingRateRepository;

    @Override
    public double calculateBill(int unitConsumption, ConnectionType connectionType) {
        double rate = billingRateRepository.findByConnectionType(connectionType)
                .orElseThrow(() -> new RuntimeException("Rate not found"))
                .getRate();
        return unitConsumption * rate;
    }
}
