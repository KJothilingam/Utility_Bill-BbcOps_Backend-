package com.BBC_Ops.BBC_Ops.Controller;

import com.BBC_Ops.BBC_Ops.Model.Customer;
import com.BBC_Ops.BBC_Ops.Model.PaymentRecord;
import com.BBC_Ops.BBC_Ops.Service.CustomerService;
import com.BBC_Ops.BBC_Ops.Service.PaymentRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
    @RequestMapping("/payment-records")
@CrossOrigin(origins = "http://localhost:4200")
public class PaymentRecordController {

    private final PaymentRecordService paymentRecordService;
    private final CustomerService customerService; // Add CustomerService to fetch customer details

    public PaymentRecordController(PaymentRecordService paymentRecordService, CustomerService customerService) {
        this.paymentRecordService = paymentRecordService;
        this.customerService = customerService;
    }

    // ✅ Fetch all payment records
    @GetMapping
    public ResponseEntity<List<PaymentRecord>> getAllPaymentRecords() {
        List<PaymentRecord> records = paymentRecordService.getAllPaymentRecords();
        return ResponseEntity.ok(records);
    }

    // Fetch payment records by customer ID
    @GetMapping("/customer")
    public ResponseEntity<List<PaymentRecord>> getPaymentRecordsByCustomerId(@RequestParam("customerId") Long customerId) {
        // Fetch the customer by ID
        Optional<Customer> customer = customerService.getCustomerById(customerId);

        if (!customer.isPresent()) {
            return ResponseEntity.notFound().build(); // Return 404 if customer is not found
        }

        // Fetch payment records by meter number (from Customer entity)
        List<PaymentRecord> records = paymentRecordService.getPaymentRecordsByMeterNumber(customer.get().getMeterNumber());
        return ResponseEntity.ok(records);
    }
}