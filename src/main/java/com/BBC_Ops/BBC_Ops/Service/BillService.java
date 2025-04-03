package com.BBC_Ops.BBC_Ops.Service;
import com.BBC_Ops.BBC_Ops.Utils.MonthlyPaymentDTO;
import com.BBC_Ops.BBC_Ops.Utils.PaymentSummaryDTO;
import org.springframework.data.domain.Pageable;

import com.BBC_Ops.BBC_Ops.Enum.PaymentStatus;
import com.BBC_Ops.BBC_Ops.Exceptions.CustomerNotFoundException;
import com.BBC_Ops.BBC_Ops.Model.Bill;
import com.BBC_Ops.BBC_Ops.Model.Customer;
import com.BBC_Ops.BBC_Ops.Repository.BillRepository;
import com.BBC_Ops.BBC_Ops.Repository.CustomerRepository;
import com.BBC_Ops.BBC_Ops.Service.BillingContext;
import com.BBC_Ops.BBC_Ops.Service.DiscountedBillingStrategy;
import com.BBC_Ops.BBC_Ops.Service.PeakHourBillingStrategy;
import com.BBC_Ops.BBC_Ops.Service.StandardBillingStrategy;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BillService {

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

    /** ✅ Runs at startup */
    @PostConstruct
    public void checkAndUpdateOverdueBills() {
        System.out.println("Checking overdue bills...");
        updateOverdueBills();
    }


    /** ✅ Runs every day at midnight to update overdue bills */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void updateOverdueBills() {
        billRepository.updateOverdueBills();
        System.out.println("Overdue bills updated successfully.");
    }

    public Bill generateBill(String meterNumber, int unitConsumed, Date monthDate) {
        // ✅ Debugging Log - Check incoming parameters
        System.out.println("Generating bill for Meter: " + meterNumber +
                ", Unit Consumed: " + unitConsumed +
                ", Month Date: " + monthDate);

        // ✅ Validate unit consumption
        if (unitConsumed <= 0) {
            throw new IllegalArgumentException("Invalid Unit Consumed: " + unitConsumed);
        }

        // ✅ Fetch customer based on meter number
        Optional<Customer> optionalCustomer = customerRepository.findByMeterNumber(meterNumber);
        if (optionalCustomer.isEmpty()) {
            throw new CustomerNotFoundException("Invalid meter number: " + meterNumber);
        }
        Customer customer = optionalCustomer.get();

        // ✅ Check if a bill already exists for this customer and month
        boolean billExists = billRepository.existsByCustomerAndMonthDate(customer, monthDate);
        if (billExists) {
            throw new IllegalStateException("Bill already generated for this month: " + monthDate);
        }

        // ✅ Select billing strategy
        if (isPeakHours()) {
            billingContext.setBillingStrategy(peakHourBillingStrategy);
        } else if (isEligibleForDiscount(customer)) {
            billingContext.setBillingStrategy(discountedBillingStrategy);
        } else {
            billingContext.setBillingStrategy(standardBillingStrategy);
        }

        // ✅ Calculate total bill amount
        double totalBillAmount = billingContext.generateBill(unitConsumed);

        // ✅ Set due date (10 days after monthDate)
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(monthDate);
        calendar.add(Calendar.DAY_OF_MONTH, 10);
        Date dueDate = calendar.getTime();

        // ✅ Create and Save Bill Object
        Bill bill = new Bill();
        bill.setCustomer(customer);
        bill.setInvoiceId(UUID.randomUUID().toString());
        bill.setMonthDate(monthDate);
        bill.setTotalBillAmount(totalBillAmount);
        bill.setPaymentStatus(PaymentStatus.PENDING);
        bill.setCreatedAt(new Date());  // ✅ Set actual creation time
        bill.setDueDate(dueDate);
        bill.setUnitConsumed(unitConsumed);  // ✅ Ensure unitConsumed is set before saving

        Bill savedBill = billRepository.save(bill);

        // ✅ Reset unit consumption after saving the bill
        customer.setUnitConsumption(0);
        customerRepository.save(customer);

        // ✅ Logging for Debugging
        System.out.println("✅ Bill Generated Successfully!");
        System.out.println("Invoice ID: " + bill.getInvoiceId());
        System.out.println("Billing Month: " + monthDate);
        System.out.println("Due Date: " + dueDate);
        System.out.println("Total Amount: Rs. " + totalBillAmount);

        return savedBill;
    }

    private boolean isPeakHours() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour >= 18 && hour < 22; // Peak hours: 6PM - 10PM
    }

    private boolean isEligibleForDiscount(Customer customer) {
        return customer.getEmail().endsWith("@example.com"); // Example: Apply discount for specific customers
    }


    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

//    public List<Bill> getUnpaidBillsByMeterNumber(String meterNumber) {
//        return billRepository.findByCustomer_MeterNumberAndPaymentStatus(meterNumber, PaymentStatus.PENDING);
//    }
    public List<Bill> getUnpaidBillsByMeterNumber(String meterNumber) {
        return billRepository.findByCustomer_MeterNumberAndPaymentStatusIn(meterNumber,
                Arrays.asList(PaymentStatus.PENDING, PaymentStatus.OVERDUE));
    }
    public List<Bill> getRecentPendingBills(int limit) {
        List<Bill> pendingBills = billRepository.findTopRecentPendingBills();
        return pendingBills.stream().limit(limit).collect(Collectors.toList()); // Apply limit manually
    }
    public List<Bill> getBillsByCustomerId(Long customerId) {
        return billRepository.findByCustomer_CustomerId(customerId);
    }
    public List<Bill> getOverdueBills() {
        return billRepository.findOverdueBills();
    }

    public PaymentSummaryDTO getPaymentSummary() {
        long pending = billRepository.countPendingPayments();
        long paid = billRepository.countPaidPayments();
        long overdue = billRepository.countOverduePayments();
        return new PaymentSummaryDTO(pending, paid, overdue);
    }

    public MonthlyPaymentDTO getMonthlyPayments() {
        List<Object[]> results = billRepository.getMonthlyPayments();
        List<String> months = results.stream().map(r -> (String) r[0]).collect(Collectors.toList());
        List<Double> amounts = results.stream().map(r -> (Double) r[1]).collect(Collectors.toList());

        return new MonthlyPaymentDTO(months, amounts);
    }

    public Bill getBillByInvoiceId(String invoiceId) {
        Optional<Bill> billOptional = billRepository.findByInvoiceId(invoiceId);
        return billOptional.orElse(null);
    }

    public Map<String, List<Integer>> getMonthlyStats(Long customerId) {
        List<Object[]> results = billRepository.getMonthlyStatsByCustomer(customerId);

        List<Integer> payments = new ArrayList<>();
        List<Integer> units = new ArrayList<>();
        List<Integer> months = new ArrayList<>();

        for (Object[] result : results) {
            months.add(((Number) result[0]).intValue());
            payments.add(((Number) result[1]).intValue());
            units.add(((Number) result[2]).intValue());
        }

        Map<String, List<Integer>> stats = new HashMap<>();
        stats.put("months", months);
        stats.put("payments", payments);
        stats.put("units", units);

        return stats;
    }
}
