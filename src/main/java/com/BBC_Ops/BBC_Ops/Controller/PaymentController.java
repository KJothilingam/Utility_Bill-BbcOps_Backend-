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


    @PostMapping("/customer/process")
    public PaymentResponse processPaymentByCustomer(@RequestBody PaymentRequest paymentRequest) {
        return paymentService.processPaymentbyCustomer(paymentRequest);
    }


}