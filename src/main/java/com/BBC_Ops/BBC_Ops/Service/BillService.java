package com.BBC_Ops.BBC_Ops.Service;

import com.BBC_Ops.BBC_Ops.Service.BillingStrategy.BillingContext;
import com.BBC_Ops.BBC_Ops.Service.BillingStrategy.DiscountedBillingStrategy;
import com.BBC_Ops.BBC_Ops.Service.BillingStrategy.PeakHourBillingStrategy;
import com.BBC_Ops.BBC_Ops.Service.BillingStrategy.StandardBillingStrategy;
import com.BBC_Ops.BBC_Ops.Utils.MonthlyPaymentDTO;
import com.BBC_Ops.BBC_Ops.Utils.PaymentSummaryDTO;
import com.BBC_Ops.BBC_Ops.Enum.PaymentStatus;
import com.BBC_Ops.BBC_Ops.Exceptions.CustomerNotFoundException;
import com.BBC_Ops.BBC_Ops.Model.Bill;
import com.BBC_Ops.BBC_Ops.Model.Customer;
import com.BBC_Ops.BBC_Ops.Repository.BillRepository;
import com.BBC_Ops.BBC_Ops.Repository.CustomerRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BillService {

    private static final Logger logger = LoggerFactory.getLogger(BillService.class);

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BillingContext billingContext;

    @Autowired
    private StandardBillingStrategy standardBillingStrategy;

    @Autowired
    private DiscountedBillingStrategy discountedBillingStrategy;

    @Autowired
    private PeakHourBillingStrategy peakHourBillingStrategy;

    @PostConstruct
    public void checkAndUpdateOverdueBills() {
        logger.info("Starting application... checking for overdue bills.");
        updateOverdueBills();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void updateOverdueBills() {
        billRepository.updateOverdueBills();
        logger.info("Overdue bills updated successfully.");
    }

    public Bill generateBill(String meterNumber, int unitConsumed, Date monthDate) {
        logger.info("Generating bill for meterNumber={}, unitConsumed={}, monthDate={}",
                meterNumber, unitConsumed, monthDate);

        if (unitConsumed <= 0) {
            logger.warn("Invalid unit consumption: {}", unitConsumed);
            throw new IllegalArgumentException("Invalid Unit Consumed: " + unitConsumed);
        }

        Optional<Customer> optionalCustomer = customerRepository.findByMeterNumber(meterNumber);
        if (optionalCustomer.isEmpty()) {
            logger.error("Customer not found for meter number: {}", meterNumber);
            throw new CustomerNotFoundException("Invalid meter number: " + meterNumber);
        }

        Customer customer = optionalCustomer.get();

        if (billRepository.existsByCustomerAndMonth(customer, monthDate)) {
            logger.warn("Bill already exists for customer={} and month={}", customer.getCustomerId(), monthDate);
            throw new IllegalStateException("Bill already generated for this month and meter number.");
        }

        if (isPeakHours()) {
            billingContext.setBillingStrategy(peakHourBillingStrategy);
            logger.info("Using PeakHourBillingStrategy.");
        } else if (isEligibleForDiscount(customer)) {
            billingContext.setBillingStrategy(discountedBillingStrategy);
            logger.info("Using DiscountedBillingStrategy for customer={}", customer.getCustomerId());
        } else {
            billingContext.setBillingStrategy(standardBillingStrategy);
            logger.info("Using StandardBillingStrategy.");
        }

        double totalBillAmount = billingContext.generateBill(unitConsumed);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(monthDate);
        calendar.add(Calendar.DAY_OF_MONTH, 10);
        Date dueDate = calendar.getTime();

        Bill bill = new Bill();
        bill.setCustomer(customer);
        bill.setInvoiceId(UUID.randomUUID().toString());
        bill.setMonthDate(monthDate);
        bill.setTotalBillAmount(totalBillAmount);
        bill.setPaymentStatus(PaymentStatus.PENDING);
        bill.setCreatedAt(new Date());
        bill.setDueDate(dueDate);
        bill.setUnitConsumed(unitConsumed);

        Bill savedBill = billRepository.save(bill);

        customer.setUnitConsumption(0);
        customerRepository.save(customer);

        logger.info("✅ Bill Generated: invoiceId={}, customerId={}, amount={}, dueDate={}",
                bill.getInvoiceId(), customer.getCustomerId(), totalBillAmount, dueDate);

        return savedBill;
    }

    private boolean isPeakHours() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return hour >= 9 && hour < 12;
    }

    private boolean isEligibleForDiscount(Customer customer) {
        return customer.getEmail().endsWith("@example.com");
    }

    public List<Bill> getAllBills() {
        logger.info("Fetching all bills...");
        return billRepository.findAll();
    }

    public List<Bill> getUnpaidBillsByMeterNumber(String meterNumber) {
        logger.info("Fetching unpaid bills for meterNumber={}", meterNumber);
        return billRepository.findByCustomer_MeterNumberAndPaymentStatusIn(meterNumber,
                Arrays.asList(PaymentStatus.PENDING, PaymentStatus.OVERDUE));
    }

    public List<Bill> getRecentPendingBills(int limit) {
        logger.info("Fetching top {} recent pending bills", limit);
        List<Bill> pendingBills = billRepository.findTopRecentPendingBills();
        return pendingBills.stream().limit(limit).collect(Collectors.toList());
    }

    public List<Bill> getBillsByCustomerId(Long customerId) {
        logger.info("Fetching bills for customerId={}", customerId);
        return billRepository.findByCustomer_CustomerId(customerId);
    }

    public List<Bill> getOverdueBills() {
        logger.info("Fetching overdue bills...");
        return billRepository.findOverdueBills();
    }

    public PaymentSummaryDTO getPaymentSummary() {
        logger.info("Getting payment summary...");
        long pending = billRepository.countPendingPayments();
        long paid = billRepository.countPaidPayments();
        long overdue = billRepository.countOverduePayments();
        logger.debug("Summary: pending={}, paid={}, overdue={}", pending, paid, overdue);
        return new PaymentSummaryDTO(pending, paid, overdue);
    }

    public MonthlyPaymentDTO getMonthlyPayments() {
        logger.info("Getting monthly payment statistics...");
        List<Object[]> results = billRepository.getMonthlyPayments();
        List<String> months = results.stream().map(r -> (String) r[0]).collect(Collectors.toList());
        List<Double> amounts = results.stream().map(r -> (Double) r[1]).collect(Collectors.toList());
        return new MonthlyPaymentDTO(months, amounts);
    }

    public Bill getBillByInvoiceId(String invoiceId) {
        logger.info("Fetching bill by invoiceId={}", invoiceId);
        return billRepository.findByInvoiceId(invoiceId).orElse(null);
    }

    public Map<String, List<Integer>> getMonthlyStats(Long customerId) {
        logger.info("Getting monthly stats for customerId={}", customerId);
        List<Object[]> results = billRepository.getMonthlyStatsByCustomer(customerId);

        List<Integer> payments = new ArrayList<>();
        List<Integer> units = new ArrayList<>();
        List<Integer> months = new ArrayList<>();

        for (Object[] result : results) {
            months.add(((Number) result[0]).intValue());
            payments.add(((Number) result[1]).intValue());
            units.add(((Number) result[2]).intValue());
        }

        logger.debug("Monthly stats - months: {}, payments: {}, units: {}", months, payments, units);

        Map<String, List<Integer>> stats = new HashMap<>();
        stats.put("months", months);
        stats.put("payments", payments);
        stats.put("units", units);
        return stats;
    }
}
