package com.BBC_Ops.BBC_Ops.Service;

import com.BBC_Ops.BBC_Ops.Model.PaymentRecord;
import com.BBC_Ops.BBC_Ops.Repository.PaymentRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    // Fetch payment records by meter number
    public List<PaymentRecord> getPaymentRecordsByMeterNumber(String meterNumber) {
        return paymentRecordRepository.findByMeterNumber(meterNumber);
    }

    // Fetch payment record by transaction ID
    public Optional<PaymentRecord> getPaymentRecordByTransactionId(String transactionId) {
        return paymentRecordRepository.findByTransactionId(transactionId);
    }
}
