//package com.BBC_Ops.BBC_Ops.Controller;
//
//import com.BBC_Ops.BBC_Ops.Model.Bill;
//import com.BBC_Ops.BBC_Ops.Model.Transaction;
//import com.BBC_Ops.BBC_Ops.Service.PaymentService;
//import com.BBC_Ops.BBC_Ops.Utils.PaymentRequest;
//import com.BBC_Ops.BBC_Ops.Utils.PaymentResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/payments")
//public class PaymentController {
//
//    @Autowired
//    private PaymentService paymentService;
//
//    @PostMapping("/process")
//    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
//        Transaction transaction = paymentService.processPayment(
//                request.getBillId(), request.getAmount(), request.getPaymentMethod()
//        );
//
//        Bill bill = transaction.getBill();
//
//        PaymentResponse response = new PaymentResponse(
//                true,
//                "Payment successful!",
//                bill.getInvoiceId(),
//                bill.getMeterNumber(),
//                bill.getUnitConsumed(),
//                bill.getDueDate(),
//                bill.getTotalBillAmount(),
//                transaction.getAmountPaid(),
//                transaction.getDiscountApplied(),
//                transaction.getAmountPaid(),
//                transaction.getPaymentMethod(),
//                transaction.getPaymentDate()
//        );
//
//        return ResponseEntity.ok(response);
//    }
//}
