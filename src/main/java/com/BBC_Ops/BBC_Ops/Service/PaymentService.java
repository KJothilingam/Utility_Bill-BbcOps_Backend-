//package com.BBC_Ops.BBC_Ops.Service;
//
//import com.BBC_Ops.BBC_Ops.Enum.PaymentMethod;
//import com.BBC_Ops.BBC_Ops.Enum.PaymentStatus;
//import com.BBC_Ops.BBC_Ops.Enum.TransactionStatus;
//import com.BBC_Ops.BBC_Ops.Model.Bill;
//import com.BBC_Ops.BBC_Ops.Model.Transaction;
//import com.BBC_Ops.BBC_Ops.Repository.BillRepository;
//import com.BBC_Ops.BBC_Ops.Repository.TransactionRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.Date;
//import java.util.Optional;
//
//@Service
//public class PaymentService {
//
//    @Autowired
//    private TransactionRepository transactionRepository;
//
//    @Autowired
//    private BillRepository billRepository;
//
//    @Autowired
//    private DiscountContext discountContext;
//
//    public Transaction processPayment(Long billId, Double amount, PaymentMethod paymentMethod) {
//        Optional<Bill> billOpt = billRepository.findById(billId);
//        if (billOpt.isEmpty()) {
//            throw new IllegalArgumentException("Invalid Bill ID");
//        }
//
//        Bill bill = billOpt.get();
//        if (bill.getPaymentStatus() == PaymentStatus.PAID) {
//            throw new IllegalStateException("Bill already paid.");
//        }
//
//        if (amount <= 0) {
//            throw new IllegalArgumentException("Payment amount must be greater than zero.");
//        }
//
//        double discount = discountContext.calculateDiscount(bill, paymentMethod);
//        double finalAmount = amount - discount;
//
//        // Create and save transaction
//        Transaction transaction = new Transaction();
//        transaction.setBill(bill);
//        transaction.setCustomer(bill.getCustomer());
//        transaction.setAmountPaid(finalAmount);
//        transaction.setPaymentDate(new Date());
//        transaction.setPaymentMethod(paymentMethod);
//        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
//        transaction.setPaymentStatus(PaymentStatus.PAID);
//        transaction.setDiscountApplied(discount);
//
//        Transaction savedTransaction = transactionRepository.save(transaction);
//
//        // Update bill status
//        bill.setPaymentStatus(PaymentStatus.PAID);
//        billRepository.save(bill);
//
//        return savedTransaction;
//    }
//}
