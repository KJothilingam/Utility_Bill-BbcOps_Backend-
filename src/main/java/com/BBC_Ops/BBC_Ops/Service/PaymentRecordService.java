package com.BBC_Ops.BBC_Ops.Service;

import com.BBC_Ops.BBC_Ops.Model.PaymentRecord;
import com.BBC_Ops.BBC_Ops.Repository.PaymentRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentRecordService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentRecordService.class);

    @Autowired
    private PaymentRecordRepository paymentRecordRepository;

    public void savePaymentRecord(PaymentRecord record) {
        logger.info("Saving payment record: {}", record);
        paymentRecordRepository.save(record);
        logger.debug("Payment record saved successfully for meter number: {}", record.getMeterNumber());
    }

    public List<PaymentRecord> getAllPaymentRecords() {
        logger.info("Fetching all payment records...");
        List<PaymentRecord> records = paymentRecordRepository.findAll();
        logger.debug("Total payment records fetched: {}", records.size());
        return records;
    }

    public List<PaymentRecord> getPaymentRecordsByMeterNumber(String meterNumber) {
        logger.info("Fetching payment records by meter number: {}", meterNumber);
        List<PaymentRecord> records = paymentRecordRepository.findByMeterNumber(meterNumber);
        logger.debug("Fetched {} records for meter number: {}", records.size(), meterNumber);
        return records;
    }

    public Optional<PaymentRecord> getPaymentRecordByTransactionId(String transactionId) {
        logger.info("Fetching payment record by transaction ID: {}", transactionId);
        Optional<PaymentRecord> record = paymentRecordRepository.findByTransactionId(transactionId);
        if (record.isPresent()) {
            logger.debug("Payment record found for transaction ID: {}", transactionId);
        } else {
            logger.warn("No payment record found for transaction ID: {}", transactionId);
        }
        return record;
    }
}
