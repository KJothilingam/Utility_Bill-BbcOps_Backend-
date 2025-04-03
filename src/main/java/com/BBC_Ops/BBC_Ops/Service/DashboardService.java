package com.BBC_Ops.BBC_Ops.Service;

import com.BBC_Ops.BBC_Ops.Model.PaymentRecord;
import com.BBC_Ops.BBC_Ops.Repository.BillRepository;
import com.BBC_Ops.BBC_Ops.Repository.CustomerRepository;
import com.BBC_Ops.BBC_Ops.Repository.PaymentRecordRepository;
import com.BBC_Ops.BBC_Ops.Utils.DashboardResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PaymentRecordRepository paymentRecordRepository;

    @Autowired
    private BillRepository billRepository;

    public DashboardResponse getDashboardStats() {
        long totalCustomers = customerRepository.countTotalCustomers();
        Double totalPayments = paymentRecordRepository.getTotalPaymentsForCurrentMonth();
        long pendingPayments = billRepository.countPendingAndOverdueBills();

        return new DashboardResponse(totalCustomers, totalPayments != null ? totalPayments : 0.0, pendingPayments);
    }


    public List<PaymentRecord> getLatestPayments() {
        return paymentRecordRepository.findLatestPayments(PageRequest.of(0, 5)).getContent();
    }

    public List<PaymentRecord> getLatestPaymentsByCustomer(String meterNumber) {
        return paymentRecordRepository.findByMeterNumberOrderByPaymentDateDesc(meterNumber, PageRequest.of(0, 5));
    }
}

