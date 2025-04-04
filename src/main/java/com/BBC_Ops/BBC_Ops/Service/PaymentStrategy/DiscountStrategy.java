package com.BBC_Ops.BBC_Ops.Service.PaymentStrategy;

import com.BBC_Ops.BBC_Ops.Model.Bill;
import com.BBC_Ops.BBC_Ops.Enum.PaymentMethod;

public interface DiscountStrategy {
    double calculateDiscount(Bill bill, PaymentMethod paymentMethod);
}
