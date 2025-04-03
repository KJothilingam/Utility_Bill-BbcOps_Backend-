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

//    @Transactional
//    public PaymentResponse processPayment(PaymentRequest request) {
//        Bill bill = billRepository.findById(request.getBillId()).orElse(null);
//        if (bill == null) {
//            return new PaymentResponse(false, "Bill not found for ID: " + request.getBillId(), null, null, 0, null, 0, 0, 0, 0, null, null, null, null);
//        }
//
//        if (bill.getPaymentStatus() == PaymentStatus.PAID) {
//            return new PaymentResponse(false, "Bill is already paid", null, null, 0, null, 0, 0, 0, 0, null, null, null, null);
//        }
//
//        PaymentMethod method = request.getPaymentMethod();
//        boolean success = false;
//        double discount = discountContext.calculateDiscount(bill, method);
//        double discountedAmount = request.getAmount() - discount;
//
//        if (method == PaymentMethod.CASH) {
//            success = true;
//            bill.setPaymentStatus(PaymentStatus.PAID);
//            billRepository.save(bill);
//        } else {
//            Wallet wallet = walletRepository.findByCustomer_CustomerId(bill.getCustomer().getCustomerId());
//            if (wallet == null) {
//                return new PaymentResponse(false, "Wallet not found for customer ID: " + bill.getCustomer().getCustomerId(), null, null, 0, null, 0, 0, 0, 0, null, null, null, null);
//            }
//
//            switch (method) {
//                case CREDIT_CARD:
//                    if (wallet.getCreditCardBalance() >= discountedAmount) {
//                        wallet.setCreditCardBalance(wallet.getCreditCardBalance() - discountedAmount);
//                        success = true;
//                    }
//                    break;
//                case DEBIT_CARD:
//                    if (wallet.getDebitCardBalance() >= discountedAmount) {
//                        wallet.setDebitCardBalance(wallet.getDebitCardBalance() - discountedAmount);
//                        success = true;
//                    }
//                    break;
//                case WALLET:
//                    if (wallet.getWalletBalance() >= discountedAmount) {
//                        wallet.setWalletBalance(wallet.getWalletBalance() - discountedAmount);
//                        success = true;
//                    }
//                    break;
//                case UPI:
//                    if (wallet.getUpiBalance() >= discountedAmount) {
//                        wallet.setUpiBalance(wallet.getUpiBalance() - discountedAmount);
//                        success = true;
//                    }
//                    break;
//                default:
//                    return new PaymentResponse(false, "Invalid payment method", null, null, 0, null, 0, 0, 0, 0, null, null, null, null);
//            }
//
//            if (!success) {
//                return new PaymentResponse(false, "Insufficient balance in " + method + " account!", null, null, 0, null, 0, 0, 0, 0, null, null, null, null);
//            }
//
//            walletRepository.save(wallet);
//        }
//
//        Transaction transaction = new Transaction();
//        transaction.setBill(bill);
//        transaction.setCustomer(bill.getCustomer());
//        transaction.setAmountPaid(discountedAmount);
//        transaction.setDiscountApplied(discount);
//        transaction.setFinalAmountPaid(discountedAmount);
//        transaction.setPaymentMethod(method);
//        transaction.setPaymentDate(new Date());
//        transaction.setStatus(TransactionStatus.SUCCESS);
//
//        Transaction savedTransaction = null;
//        try {
//            savedTransaction = transactionRepository.save(transaction);
//            bill.setPaymentStatus(PaymentStatus.PAID);
//            billRepository.save(bill);
//        } catch (Exception e) {
//            return new PaymentResponse(false, "Transaction failed! " + e.getMessage(), null, null, 0, null, 0, 0, 0, 0, null, null, null, null);
//        }
//
//        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
//        String billingMonth = bill.getMonthDate() != null ? sdf.format(bill.getMonthDate()) : "Unknown";
//
//        return new PaymentResponse(
//                true, "Payment successful!",
//                bill.getInvoiceId(),
//                bill.getCustomer().getMeterNumber(),
//                bill.getUnitConsumed(),
//                bill.getDueDate(),
//                bill.getTotalBillAmount(),
//                request.getAmount(),
//                discount,
//                discountedAmount,
//                method,
//                new Date(),
//                billingMonth,
//                String.valueOf(savedTransaction.getTransactionId())
//        );
//    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        Bill bill = billRepository.findById(request.getBillId()).orElse(null);
        if (bill == null) {
            return new PaymentResponse(false, "Bill not found for ID: " + request.getBillId(), null, null, 0, null, 0, 0, 0, 0, null, null, null, null);
        }

        if (bill.getPaymentStatus() == PaymentStatus.PAID) {
            return new PaymentResponse(false, "Bill is already paid", null, null, 0, null, 0, 0, 0, 0, null, null, null, null);
        }

        PaymentMethod method = request.getPaymentMethod();
        boolean success = false;
        double discount = discountContext.calculateDiscount(bill, method);
        double discountedAmount = request.getAmount() - discount;

        if (method == PaymentMethod.CASH) {
            // ✅ Directly mark payment as successful
            success = true;
        } else {
            // ✅ Fetch Wallet Balance (Only for non-cash payments)
            Wallet wallet = walletRepository.findByCustomer_CustomerId(bill.getCustomer().getCustomerId());
            if (wallet == null) {
                return new PaymentResponse(false, "Wallet not found for customer ID: " + bill.getCustomer().getCustomerId(), null, null, 0, null, 0, 0, 0, 0, null, null, null, null);
            }

            // ✅ Deduct balance based on payment method
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

            walletRepository.save(wallet);
        }

        // ✅ Create a transaction (for all payment methods, including CASH)
        Transaction transaction = new Transaction();
        transaction.setBill(bill);
        transaction.setCustomer(bill.getCustomer());
        transaction.setAmountPaid(discountedAmount);
        transaction.setDiscountApplied(discount);
        transaction.setFinalAmountPaid(discountedAmount);
        transaction.setPaymentMethod(method);
        transaction.setPaymentDate(new Date());
        transaction.setStatus(TransactionStatus.SUCCESS);

        Transaction savedTransaction = transactionRepository.save(transaction);

        // ✅ Mark bill as PAID
        bill.setPaymentStatus(PaymentStatus.PAID);
        billRepository.save(bill);

        // ✅ Generate response
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
        String billingMonth = bill.getMonthDate() != null ? sdf.format(bill.getMonthDate()) : "Unknown";

        return new PaymentResponse(
                true, "Payment successful!",
                bill.getInvoiceId(),
                bill.getCustomer().getMeterNumber(),
                bill.getUnitConsumed(),
                bill.getDueDate(),
                bill.getTotalBillAmount(),
                request.getAmount(),
                discount,
                discountedAmount,
                method,
                new Date(),
                billingMonth,
                String.valueOf(savedTransaction.getTransactionId())
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

            case CASH:
                    success = true;
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
