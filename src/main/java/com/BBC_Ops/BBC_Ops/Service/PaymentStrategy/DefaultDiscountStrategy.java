package com.BBC_Ops.BBC_Ops.Service.PaymentStrategy;

import com.BBC_Ops.BBC_Ops.Enum.PaymentMethod;
import com.BBC_Ops.BBC_Ops.Model.Bill;
import com.BBC_Ops.BBC_Ops.Repository.BillingModifierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class DefaultDiscountStrategy implements DiscountStrategy {

    @Autowired
    private BillingModifierRepository billingModifierRepository;

    @Override
    public double calculateDiscount(Bill bill, PaymentMethod paymentMethod) {
        double discount = 0;
        boolean isBeforeDue = bill.getDueDate().after(new Date());

        if (isBeforeDue) {
            // Fetch early payment discount
            double earlyPaymentDiscount = billingModifierRepository.findByModifierName("EARLY_PAYMENT_DISCOUNT")
                    .orElseThrow(() -> new RuntimeException("EARLY_PAYMENT_DISCOUNT not configured"))
                    .getValue();
            discount += earlyPaymentDiscount * bill.getTotalBillAmount();

            // Fetch online payment discount if applicable
            if (paymentMethod != PaymentMethod.CASH) {
                double onlinePaymentDiscount = billingModifierRepository.findByModifierName("ONLINE_PAYMENT_EXTRA_DISCOUNT")
                        .orElseThrow(() -> new RuntimeException("ONLINE_PAYMENT_EXTRA_DISCOUNT not configured"))
                        .getValue();
                discount += onlinePaymentDiscount * bill.getTotalBillAmount();
            }
        }

        return discount;
    }
}
