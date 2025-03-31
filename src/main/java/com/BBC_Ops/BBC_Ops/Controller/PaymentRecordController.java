package com.BBC_Ops.BBC_Ops.Controller;

import com.BBC_Ops.BBC_Ops.Model.PaymentRecord;
import com.BBC_Ops.BBC_Ops.Service.PaymentRecordService;
import com.BBC_Ops.BBC_Ops.Utils.PaymentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@RestController
    @RequestMapping("/payment-records")
@CrossOrigin(origins = "http://localhost:4200")
public class PaymentRecordController {

    private final PaymentRecordService paymentRecordService;

    public PaymentRecordController(PaymentRecordService paymentRecordService) {
        this.paymentRecordService = paymentRecordService;
    }


    // ✅ Fetch all payment records
    @GetMapping
    public ResponseEntity<List<PaymentRecord>> getAllPaymentRecords() {
        List<PaymentRecord> records = paymentRecordService.getAllPaymentRecords();
        return ResponseEntity.ok(records);
    }
//
//    // ✅ Fetch a specific payment record by ID
//    @GetMapping("/{id}")
//    public ResponseEntity<PaymentRecord> getPaymentRecordById(@PathVariable Long id) {
//        Optional<PaymentRecord> record = paymentRecordService.getPaymentRecordById(id);
//        return record.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    // ✅ Delete a payment record by ID
//    @DeleteMapping("/{id}")
//    public ResponseEntity<String> deletePaymentRecord(@PathVariable Long id) {
//        boolean deleted = paymentRecordService.deletePaymentRecord(id);
//        if (deleted) {
//            return ResponseEntity.ok("Payment record deleted successfully.");
//        } else {
//            return ResponseEntity.status(404).body("Payment record not found.");
//        }
//    }


}