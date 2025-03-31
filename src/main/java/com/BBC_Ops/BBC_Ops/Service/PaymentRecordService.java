package com.BBC_Ops.BBC_Ops.Service;

import com.BBC_Ops.BBC_Ops.Model.PaymentRecord;
import com.BBC_Ops.BBC_Ops.Repository.PaymentRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentRecordService {
    @Autowired
    private PaymentRecordRepository paymentRecordRepository;

    public void savePaymentRecord(PaymentRecord record) {
        paymentRecordRepository.save(record);
    }

    public List<PaymentRecord> getAllPaymentRecords() {
        return paymentRecordRepository.findAll();
    }
}
