package com.BBC_Ops.BBC_Ops.Controller;

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

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        try {
            PaymentResponse response = paymentService.processPayment(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new PaymentResponse(
                    false, e.getMessage(),
                    null, null, 0, null, 0, 0, 0, 0,
                    request.getPaymentMethod(), new Date(),
                    null, // billingMonth
                    null  // transactionId
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new PaymentResponse(
                    false, "Internal Server Error",
                    null, null, 0, null, 0, 0, 0, 0,
                    request.getPaymentMethod(), new Date(),
                    null, // billingMonth
                    null  // transactionId
            ));
        }
    }

}
