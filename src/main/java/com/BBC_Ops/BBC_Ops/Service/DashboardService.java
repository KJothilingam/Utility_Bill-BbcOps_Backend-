package com.BBC_Ops.BBC_Ops.Service;

import com.BBC_Ops.BBC_Ops.Model.PaymentRecord;
import com.BBC_Ops.BBC_Ops.Repository.BillRepository;
import com.BBC_Ops.BBC_Ops.Repository.CustomerRepository;
import com.BBC_Ops.BBC_Ops.Repository.PaymentRecordRepository;
import com.BBC_Ops.BBC_Ops.Utils.DashboardResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PaymentRecordRepository paymentRecordRepository;

    @Autowired
    private BillRepository billRepository;

    public DashboardResponse getDashboardStats() {
        logger.info("Fetching dashboard statistics...");

        long totalCustomers = customerRepository.countTotalCustomers();
        logger.debug("Total customers: {}", totalCustomers);

        Double totalPayments = paymentRecordRepository.getTotalPaymentsForCurrentMonth();
        if (totalPayments == null) {
            logger.warn("Total payments for current month returned null, setting to 0.0");
            totalPayments = 0.0;
        } else {
            logger.debug("Total payments for current month: {}", totalPayments);
        }

        long pendingPayments = billRepository.countPendingAndOverdueBills();
        logger.debug("Pending/Overdue payments count: {}", pendingPayments);

        logger.info("Dashboard statistics fetched successfully.");
        return new DashboardResponse(totalCustomers, totalPayments, pendingPayments);
    }

    public List<PaymentRecord> getLatestPayments() {
        logger.info("Fetching latest 5 payment records...");
        List<PaymentRecord> latestPayments = paymentRecordRepository.findLatestPayments(PageRequest.of(0, 5)).getContent();
        logger.debug("Latest payments fetched: {}", latestPayments.size());
        return latestPayments;
    }

    public List<PaymentRecord> getLatestPaymentsByCustomer(String meterNumber) {
        logger.info("Fetching latest 5 payment records for meter number: {}", meterNumber);
        List<PaymentRecord> customerPayments = paymentRecordRepository.findByMeterNumberOrderByPaymentDateDesc(meterNumber, PageRequest.of(0, 5));
        logger.debug("Latest payments for customer {} fetched: {}", meterNumber, customerPayments.size());
        return customerPayments;
    }
}
