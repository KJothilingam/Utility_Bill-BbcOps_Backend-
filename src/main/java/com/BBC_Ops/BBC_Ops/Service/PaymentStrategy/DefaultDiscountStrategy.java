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
        boolean isBeforeDue = bill.getDueDate().after(new Date());

        // 5% for early payment
        if (isBeforeDue) {
            discount += 0.05 * bill.getTotalBillAmount();

            // Additional 5% for online payment before due date
            if (paymentMethod != PaymentMethod.CASH) {
                discount += 0.05 * bill.getTotalBillAmount();
            }
        }

        return discount;
    }

}
