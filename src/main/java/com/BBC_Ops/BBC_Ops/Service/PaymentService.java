package com.BBC_Ops.BBC_Ops.Service;

import com.BBC_Ops.BBC_Ops.Enum.PaymentMethod;
import com.BBC_Ops.BBC_Ops.Enum.PaymentStatus;
import com.BBC_Ops.BBC_Ops.Enum.TransactionStatus;
import com.BBC_Ops.BBC_Ops.Model.Bill;
import com.BBC_Ops.BBC_Ops.Model.PaymentRecord;
import com.BBC_Ops.BBC_Ops.Model.Transaction;
import com.BBC_Ops.BBC_Ops.Model.Wallet;
import com.BBC_Ops.BBC_Ops.Repository.BillRepository;
import com.BBC_Ops.BBC_Ops.Repository.PaymentRepository;
import com.BBC_Ops.BBC_Ops.Repository.TransactionRepository;
import com.BBC_Ops.BBC_Ops.Repository.WalletRepository;
import com.BBC_Ops.BBC_Ops.Utils.PaymentRequest;
import com.BBC_Ops.BBC_Ops.Utils.PaymentResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

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


    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse processPaymentbyCustomer(PaymentRequest paymentRequest) {
        // ✅ Fetch the bill details first
        Bill bill = billRepository.findById(paymentRequest.getBillId()).orElse(null);

        if (bill == null) {
            return new PaymentResponse(false, "Bill not found for ID: " + paymentRequest.getBillId(), null,
                    null, 0, null, 0, 0, 0, 0, null, null, null, null);
        }

        // 🛑 Check if the bill is already paid
        if (bill.getPaymentStatus() == PaymentStatus.PAID) {
            return new PaymentResponse(false, "Bill is already paid", null,
                    null, 0, null, 0, 0, 0, 0, null, null, null, null);
        }

        // ✅ Now fetch the wallet using the customer ID from the bill
        Wallet wallet = walletRepository.findByCustomer_CustomerId(bill.getCustomer().getCustomerId());

        if (wallet == null) {
            return new PaymentResponse(false, "Wallet not found for customer ID: " + bill.getCustomer().getCustomerId(), null,
                    null, 0, null, 0, 0, 0, 0, null, null, null, null);
        }

        // Payment processing logic
        double amount = paymentRequest.getAmount();
        PaymentMethod method = paymentRequest.getPaymentMethod();
        boolean success = false;

        // ✅ Check sufficient balance and deduct amount from wallet
        switch (method) {
            case CREDIT_CARD:
                if (wallet.getCreditCardBalance() >= amount) {
                    wallet.setCreditCardBalance(wallet.getCreditCardBalance() - amount);
                    success = true;
                }
                break;

            case DEBIT_CARD:
                if (wallet.getDebitCardBalance() >= amount) {
                    wallet.setDebitCardBalance(wallet.getDebitCardBalance() - amount);
                    success = true;
                }
                break;

            case WALLET:
                if (wallet.getWalletBalance() >= amount) {
                    wallet.setWalletBalance(wallet.getWalletBalance() - amount);
                    success = true;
                }
                break;

            case UPI:
                if (wallet.getUpiBalance() >= amount) {
                    wallet.setUpiBalance(wallet.getUpiBalance() - amount);
                    success = true;
                }
                break;

            default:
                return new PaymentResponse(false, "Invalid payment method", null, null, 0, null, 0, 0, 0, 0, null, null, null, null);
        }

        if (!success) {
            return new PaymentResponse(false, "Insufficient balance in " + method + " account!", null, null, 0, null, 0, 0, 0, 0, null, null, null, null);
        }

        // ✅ Save the updated wallet balance
        walletRepository.save(wallet);

        // ✅ Calculate discount and final payment amount
        double discount = bill.getDiscountApplied(); // Assuming discount is already applied on the bill
        double finalAmountPaid = amount - discount;

        // ✅ Create new payment record
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setInvoiceId(UUID.randomUUID().toString());
        paymentRecord.setMeterNumber("MTR-" + bill.getBillId());
        paymentRecord.setUnitConsumed(bill.getUnitConsumed()); // ✅ Set unitConsumed from bill
        paymentRecord.setDueDate(bill.getDueDate()); // ✅ Set dueDate from bill
        paymentRecord.setTotalBillAmount(amount);
        paymentRecord.setAmountPaid(amount);
        paymentRecord.setDiscountApplied(discount); // ✅ Set discount from bill
        paymentRecord.setFinalAmountPaid(finalAmountPaid); // ✅ Calculate finalAmountPaid
        paymentRecord.setBillingMonth(formatDateToString(bill.getMonthDate())); // ✅ Set billingMonth as String
        paymentRecord.setPaymentMethod(method);
        paymentRecord.setPaymentDate(new Date());
        paymentRecord.setTransactionId(UUID.randomUUID().toString());
        paymentRepository.save(paymentRecord);

        // ✅ Update bill status to PAID
        bill.setPaymentStatus(PaymentStatus.PAID);
        billRepository.save(bill);

        // ✅ Prepare response with all details
        String transactionId = paymentRecord.getTransactionId();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
        String billingMonth = bill.getMonthDate() != null ? sdf.format(bill.getMonthDate()) : "Unknown";

        return new PaymentResponse(
                true, "Payment successful!",
                paymentRecord.getInvoiceId(),
                paymentRecord.getMeterNumber(),
                bill.getUnitConsumed(),
                bill.getDueDate(),
                amount,
                amount,
                discount,
                finalAmountPaid,
                method,
                paymentRecord.getPaymentDate(),
                billingMonth,
                transactionId
        );
    }

    // Helper method to format Date to String
    private String formatDateToString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // Or any other format you prefer
        return sdf.format(date);
    }
}
