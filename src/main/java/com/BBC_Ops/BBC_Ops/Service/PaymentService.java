package com.BBC_Ops.BBC_Ops.Service;

import com.BBC_Ops.BBC_Ops.Enum.PaymentStatus;
import com.BBC_Ops.BBC_Ops.Enum.TransactionStatus;
import com.BBC_Ops.BBC_Ops.Model.Bill;
import com.BBC_Ops.BBC_Ops.Model.Transaction;
import com.BBC_Ops.BBC_Ops.Repository.BillRepository;
import com.BBC_Ops.BBC_Ops.Repository.TransactionRepository;
import com.BBC_Ops.BBC_Ops.Utils.PaymentRequest;
import com.BBC_Ops.BBC_Ops.Utils.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private DiscountContext discountContext;

    public PaymentResponse processPayment(PaymentRequest request) {
        Optional<Bill> billOptional = billRepository.findById(request.getBillId());
        if (!billOptional.isPresent()) {
            throw new IllegalArgumentException("Bill not found");
        }

        Bill bill = billOptional.get();

        if (bill.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException("Bill is already paid");
        }

        double discount = discountContext.calculateDiscount(bill, request.getPaymentMethod());
        double finalAmountPaid = request.getAmount() - discount;

        // Creating transaction
        Transaction transaction = new Transaction();
        transaction.setBill(bill);
        transaction.setCustomer(bill.getCustomer());
        transaction.setAmountPaid(request.getAmount());
        transaction.setDiscountApplied(discount);
        transaction.setFinalAmountPaid(finalAmountPaid);
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setPaymentDate(new Date());
        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);

        // Convert transaction ID from Long to String
        String transactionId = String.valueOf(transaction.getTransactionId());

        // Format billing month as "March 2025"
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
        String billingMonth = sdf.format(bill.getMonthDate());

        return new PaymentResponse(
                true, "Payment successful",
                bill.getInvoiceId(),
                bill.getCustomer().getMeterNumber(),
                bill.getUnitConsumed(),
                bill.getDueDate(),
                bill.getTotalBillAmount(),
                request.getAmount(),
                discount,
                finalAmountPaid,
                request.getPaymentMethod(),
                new Date(),
                billingMonth, // ✅ Now a formatted String
                transactionId // ✅ Now a String
        );
    }

}
