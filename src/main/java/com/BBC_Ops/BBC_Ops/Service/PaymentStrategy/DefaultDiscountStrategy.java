package com.BBC_Ops.BBC_Ops.Service.PaymentStrategy;

import com.BBC_Ops.BBC_Ops.Model.Bill;
import com.BBC_Ops.BBC_Ops.Enum.PaymentMethod;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class DefaultDiscountStrategy implements DiscountStrategy {

    @Override
    public double calculateDiscount(Bill bill, PaymentMethod paymentMethod) {

        double discount = 0;

        // 5% discount for early payment
        if (bill.getDueDate().after(new Date())) {
            discount += 0.05 * bill.getTotalBillAmount();
        }

        // 5% discount for online payment methods
        if (paymentMethod != PaymentMethod.CASH) {
            discount += 0.05 * bill.getTotalBillAmount();
        }

        return discount;
    }
}
