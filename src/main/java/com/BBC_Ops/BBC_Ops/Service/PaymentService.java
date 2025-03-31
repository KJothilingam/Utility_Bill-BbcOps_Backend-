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

        // 🛑 Check if the bill is already paid
        if (bill.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException("Bill is already paid");
        }

        // ✅ Calculate discount and final payment amount
        double discount = discountContext.calculateDiscount(bill, request.getPaymentMethod());
        double finalAmountPaid = request.getAmount() - discount;

        // ✅ Create new transaction
        Transaction transaction = new Transaction();
        transaction.setBill(bill);
        transaction.setCustomer(bill.getCustomer());
        transaction.setAmountPaid(request.getAmount());
        transaction.setDiscountApplied(discount);
        transaction.setFinalAmountPaid(finalAmountPaid);
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setPaymentDate(new Date());

        // ✅ Handle failed transactions (e.g., insufficient amount)
        if (request.getAmount() < bill.getTotalBillAmount()) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            return new PaymentResponse(
                    false, "Payment failed: Amount is less than total bill amount",
                    null, null, 0, null, 0, 0, 0, 0,
                    request.getPaymentMethod(), new Date(),
                    null, null
            );
        }

        // ✅ Mark transaction as SUCCESS and save it
        transaction.setStatus(TransactionStatus.SUCCESS);
        Transaction savedTransaction = transactionRepository.save(transaction);

        // ✅ Update bill status to PAID
        bill.setPaymentStatus(PaymentStatus.PAID);
        billRepository.save(bill); // Ensure the updated bill status is saved

        // ✅ Prepare response with all details
        String transactionId = String.valueOf(savedTransaction.getTransactionId());

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
        String billingMonth = bill.getMonthDate() != null ? sdf.format(bill.getMonthDate()) : "Unknown";

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
                billingMonth,
                transactionId
        );
    }

}
