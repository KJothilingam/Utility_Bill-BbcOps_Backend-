package com.BBC_Ops.BBC_Ops.Service;

import com.BBC_Ops.BBC_Ops.Enum.PaymentMethod;
import com.BBC_Ops.BBC_Ops.Model.Bill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DiscountContext {

    @Autowired
    private DefaultDiscountStrategy discountStrategy;

    public double calculateDiscount(Bill bill, PaymentMethod paymentMethod) {
        return discountStrategy.calculateDiscount(bill, paymentMethod);
    }
}
