package com.BBC_Ops.BBC_Ops.Service;

import com.BBC_Ops.BBC_Ops.Enum.PaymentMethod;
import com.BBC_Ops.BBC_Ops.Enum.PaymentStatus;
import com.BBC_Ops.BBC_Ops.Enum.TransactionStatus;
import com.BBC_Ops.BBC_Ops.Model.Bill;
import com.BBC_Ops.BBC_Ops.Model.PaymentRecord;
import com.BBC_Ops.BBC_Ops.Model.Transaction;
import com.BBC_Ops.BBC_Ops.Model.Wallet;
import com.BBC_Ops.BBC_Ops.Repository.*;
import com.BBC_Ops.BBC_Ops.Utils.PaymentRequest;
import com.BBC_Ops.BBC_Ops.Utils.PaymentResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

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

        // ✅ Fetch wallet balance using customer ID
        Wallet wallet = walletRepository.findByCustomer_CustomerId(bill.getCustomer().getCustomerId());

        if (wallet == null) {
            return new PaymentResponse(false, "Wallet not found for customer ID: " + bill.getCustomer().getCustomerId(), null,
                    null, 0, null, 0, 0, 0, 0, null, null, null, null);
        }

        // Payment processing logic
        double totalAmount = paymentRequest.getAmount();
        PaymentMethod method = paymentRequest.getPaymentMethod();
        boolean success = false;

        // ✅ Calculate discount first
        double discount = discountContext.calculateDiscount(bill, paymentRequest.getPaymentMethod());
        double discountedAmount = totalAmount - discount;

        // ✅ Check sufficient balance and deduct discounted amount from wallet
        switch (method) {
            case CREDIT_CARD:
                if (wallet.getCreditCardBalance() >= discountedAmount) {
                    wallet.setCreditCardBalance(wallet.getCreditCardBalance() - discountedAmount);
                    success = true;
                }
                break;

            case DEBIT_CARD:
                if (wallet.getDebitCardBalance() >= discountedAmount) {
                    wallet.setDebitCardBalance(wallet.getDebitCardBalance() - discountedAmount);
                    success = true;
                }
                break;

            case WALLET:
                if (wallet.getWalletBalance() >= discountedAmount) {
                    wallet.setWalletBalance(wallet.getWalletBalance() - discountedAmount);
                    success = true;
                }
                break;

            case UPI:
                if (wallet.getUpiBalance() >= discountedAmount) {
                    wallet.setUpiBalance(wallet.getUpiBalance() - discountedAmount);
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

        // ✅ Calculate final payment amount (after discount)
        double finalAmountPaid = discountedAmount;

        // ✅ 1. Create a new transaction and insert into `transactions`
        Transaction transaction = new Transaction();
        transaction.setBill(bill);
        transaction.setCustomer(bill.getCustomer());
        transaction.setAmountPaid(totalAmount); // the total bill before discount
        transaction.setDiscountApplied(discount);
        transaction.setFinalAmountPaid(finalAmountPaid);
        transaction.setPaymentMethod(method);
        transaction.setPaymentDate(new Date());
        transaction.setStatus(TransactionStatus.SUCCESS);

        Transaction savedTransaction = transactionRepository.save(transaction); // ✅ Save transaction

        // ✅ 2. Create a new payment record using transaction ID
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setInvoiceId(UUID.randomUUID().toString());
        paymentRecord.setMeterNumber(bill.getCustomer().getMeterNumber()); // ✅ Use actual meter number
        paymentRecord.setUnitConsumed(bill.getUnitConsumed());
        paymentRecord.setDueDate(bill.getDueDate());
        paymentRecord.setTotalBillAmount(totalAmount);
        paymentRecord.setAmountPaid(totalAmount);
        paymentRecord.setDiscountApplied(discount);
        paymentRecord.setFinalAmountPaid(finalAmountPaid);
        paymentRecord.setBillingMonth(formatDateToString(bill.getMonthDate()));
        paymentRecord.setPaymentMethod(method);
        paymentRecord.setPaymentDate(new Date());
        paymentRecord.setTransactionId(String.valueOf(savedTransaction.getTransactionId())); // ✅ Use actual transaction_id

        paymentRepository.save(paymentRecord);

        // ✅ 3. Update bill status to PAID
        bill.setPaymentStatus(PaymentStatus.PAID);
        billRepository.save(bill);

        // ✅ Prepare response
        String transactionId = paymentRecord.getTransactionId();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
        String billingMonth = bill.getMonthDate() != null ? sdf.format(bill.getMonthDate()) : "Unknown";

        return new PaymentResponse(
                true, "Payment successful!",
                paymentRecord.getInvoiceId(),
                paymentRecord.getMeterNumber(),
                bill.getUnitConsumed(),
                bill.getDueDate(),
                totalAmount,
                totalAmount,
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


    @Autowired
    private PaymentRecordRepository paymentRecordRepository;

    public Map<String, Object> getWeeklyPayments() {
        List<Object[]> results = paymentRecordRepository.getWeeklyPayments();
        Map<String, Object> response = new HashMap<>();

        List<Integer> weeks = results.stream().map(r -> (Integer) r[0]).toList();
        List<Double> amounts = results.stream().map(r -> (Double) r[1]).toList();

        response.put("weeks", weeks);
        response.put("amounts", amounts);

        return response;
    }
}
