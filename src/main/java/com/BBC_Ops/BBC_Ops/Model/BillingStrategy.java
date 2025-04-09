package com.BBC_Ops.BBC_Ops.Model;

public interface BillingStrategy {
    double calculateBill(int unitConsumption, ConnectionType connectionType);
}
