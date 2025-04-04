package com.BBC_Ops.BBC_Ops.Service;

import com.BBC_Ops.BBC_Ops.Enum.PaymentMethod;
import com.BBC_Ops.BBC_Ops.Enum.PaymentStatus;
import com.BBC_Ops.BBC_Ops.Enum.TransactionStatus;
import com.BBC_Ops.BBC_Ops.Model.Bill;
import com.BBC_Ops.BBC_Ops.Model.PaymentRecord;
import com.BBC_Ops.BBC_Ops.Model.Transaction;
import com.BBC_Ops.BBC_Ops.Model.Wallet;
import com.BBC_Ops.BBC_Ops.Repository.*;
import com.BBC_Ops.BBC_Ops.Service.PaymentStrategy.DiscountContext;
import com.BBC_Ops.BBC_Ops.Utils.PaymentRequest;
import com.BBC_Ops.BBC_Ops.Utils.PaymentResponse;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private DiscountContext discountContext;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentRecordRepository paymentRecordRepository;

    @Transactional
    public PaymentResponse processPaymentbyCustomer(PaymentRequest paymentRequest) {
        logger.info("Processing payment for Bill ID: {}", paymentRequest.getBillId());

        Bill bill = billRepository.findById(paymentRequest.getBillId()).orElse(null);

        if (bill == null) {
            logger.error("Bill not found for ID: {}", paymentRequest.getBillId());
            return new PaymentResponse(false, "Bill not found for ID: " + paymentRequest.getBillId(), null,
                    null, 0, null, 0, 0, 0, 0, null, null, null, null);
        }

        if (bill.getPaymentStatus() == PaymentStatus.PAID) {
            logger.warn("Bill ID {} is already paid.", paymentRequest.getBillId());
            return new PaymentResponse(false, "Bill is already paid", null,
                    null, 0, null, 0, 0, 0, 0, null, null, null, null);
        }

        Wallet wallet = walletRepository.findByCustomer_CustomerId(bill.getCustomer().getCustomerId());
        if (wallet == null) {
            logger.error("Wallet not found for customer ID: {}", bill.getCustomer().getCustomerId());
            return new PaymentResponse(false, "Wallet not found for customer ID: " + bill.getCustomer().getCustomerId(), null,
                    null, 0, null, 0, 0, 0, 0, null, null, null, null);
        }

        double totalAmount = paymentRequest.getAmount();
        PaymentMethod method = paymentRequest.getPaymentMethod();
        boolean success = false;

        double discount = discountContext.calculateDiscount(bill, method);
        double discountedAmount = totalAmount - discount;

        logger.info("Discount applied: {}, Final amount to pay: {}", discount, discountedAmount);

        switch (method) {
            case CREDIT_CARD:
                if (wallet.getCreditCardBalance() >= discountedAmount) {
                    wallet.setCreditCardBalance(wallet.getCreditCardBalance() - discountedAmount);
                    success = true;
                    logger.info("Credit card payment successful for amount: {}", discountedAmount);
                }
                break;
            case DEBIT_CARD:
                if (wallet.getDebitCardBalance() >= discountedAmount) {
                    wallet.setDebitCardBalance(wallet.getDebitCardBalance() - discountedAmount);
                    success = true;
                    logger.info("Debit card payment successful for amount: {}", discountedAmount);
                }
                break;
            case WALLET:
                if (wallet.getWalletBalance() >= discountedAmount) {
                    wallet.setWalletBalance(wallet.getWalletBalance() - discountedAmount);
                    success = true;
                    logger.info("Wallet payment successful for amount: {}", discountedAmount);
                }
                break;
            case CASH:
                success = true;
                logger.info("Cash payment selected, bypassing balance check.");
                break;
            case UPI:
                if (wallet.getUpiBalance() >= discountedAmount) {
                    wallet.setUpiBalance(wallet.getUpiBalance() - discountedAmount);
                    success = true;
                    logger.info("UPI payment successful for amount: {}", discountedAmount);
                }
                break;
            default:
                logger.error("Invalid payment method: {}", method);
                return new PaymentResponse(false, "Invalid payment method", null, null, 0, null, 0, 0, 0, 0, null, null, null, null);
        }

        if (!success) {
            logger.warn("Insufficient balance for method: {}", method);
            return new PaymentResponse(false, "Insufficient balance in " + method + " account!", null, null, 0, null, 0, 0, 0, 0, null, null, null, null);
        }

        walletRepository.save(wallet);

        double finalAmountPaid = discountedAmount;

        Transaction transaction = new Transaction();
        transaction.setBill(bill);
        transaction.setCustomer(bill.getCustomer());
        transaction.setAmountPaid(totalAmount);
        transaction.setDiscountApplied(discount);
        transaction.setFinalAmountPaid(finalAmountPaid);
        transaction.setPaymentMethod(method);
        transaction.setPaymentDate(new Date());
        transaction.setStatus(TransactionStatus.SUCCESS);

        Transaction savedTransaction = transactionRepository.save(transaction);
        logger.info("Transaction saved with ID: {}", savedTransaction.getTransactionId());

        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setInvoiceId(UUID.randomUUID().toString());
        paymentRecord.setMeterNumber(bill.getCustomer().getMeterNumber());
        paymentRecord.setUnitConsumed(bill.getUnitConsumed());
        paymentRecord.setDueDate(bill.getDueDate());
        paymentRecord.setTotalBillAmount(totalAmount);
        paymentRecord.setAmountPaid(totalAmount);
        paymentRecord.setDiscountApplied(discount);
        paymentRecord.setFinalAmountPaid(finalAmountPaid);
        paymentRecord.setBillingMonth(formatDateToString(bill.getMonthDate()));
        paymentRecord.setPaymentMethod(method);
        paymentRecord.setPaymentDate(new Date());
        paymentRecord.setTransactionId(String.valueOf(savedTransaction.getTransactionId()));

        paymentRepository.save(paymentRecord);
        logger.info("Payment record saved with Transaction ID: {}", paymentRecord.getTransactionId());

        bill.setPaymentStatus(PaymentStatus.PAID);
        billRepository.save(bill);
        logger.info("Bill ID {} marked as PAID.", bill.getBillId());

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

    private String formatDateToString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    public Map<String, Object> getWeeklyPayments() {
        logger.info("Fetching weekly payment stats from database.");
        List<Object[]> results = paymentRecordRepository.getWeeklyPayments();
        Map<String, Object> response = new HashMap<>();

        List<Integer> weeks = results.stream().map(r -> (Integer) r[0]).toList();
        List<Double> amounts = results.stream().map(r -> (Double) r[1]).toList();

        logger.info("Weekly payment data retrieved: Weeks: {}, Amounts: {}", weeks, amounts);

        response.put("weeks", weeks);
        response.put("amounts", amounts);

        return response;
    }
}
