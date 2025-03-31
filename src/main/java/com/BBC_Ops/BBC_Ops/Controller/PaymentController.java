package com.BBC_Ops.BBC_Ops.Controller;

import com.BBC_Ops.BBC_Ops.Model.PaymentRecord;
import com.BBC_Ops.BBC_Ops.Service.PaymentRecordService;
import com.BBC_Ops.BBC_Ops.Service.PaymentService;
import com.BBC_Ops.BBC_Ops.Utils.PaymentRequest;
import com.BBC_Ops.BBC_Ops.Utils.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRecordService paymentRecordService;

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        try {
            PaymentResponse response = paymentService.processPayment(request);

            if (response.isSuccess()) {
                PaymentRecord record = new PaymentRecord();
                record.setInvoiceId(response.getInvoiceId());
                record.setMeterNumber(response.getMeterNumber());
                record.setUnitConsumed(response.getUnitConsumed());
                record.setDueDate(response.getDueDate());
                record.setTotalBillAmount(response.getTotalBillAmount());
                record.setAmountPaid(response.getAmountPaid());
                record.setDiscountApplied(response.getDiscountApplied());
                record.setFinalAmountPaid(response.getFinalAmountPaid());
                record.setPaymentMethod(response.getPaymentMethod());
                record.setPaymentDate(response.getPaymentDate());
                record.setBillingMonth(response.getBillingMonth());
                record.setTransactionId(response.getTransactionId());

                paymentRecordService.savePaymentRecord(record);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new PaymentResponse(
                    false, "Internal Server Error",
                    null, null, 0, null, 0, 0, 0, 0,
                    request.getPaymentMethod(), new Date(),
                    null, null
            ));
        }
    }
}