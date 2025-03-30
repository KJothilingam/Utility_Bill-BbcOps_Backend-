package com.BBC_Ops.BBC_Ops.Service;

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

import java.util.*;

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
        Optional<Customer> optionalCustomer = customerRepository.findByMeterNumber(meterNumber);
        if (optionalCustomer.isEmpty()) {
            throw new CustomerNotFoundException("Invalid meter number: " + meterNumber);
        }

        Customer customer = optionalCustomer.get();

        // ✅ Check if a bill already exists for the given customer and month
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

        double totalBillAmount = billingContext.generateBill(unitConsumed);

        // ✅ Set due date (10 days after `monthDate`, not today's date!)
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(monthDate);  // ✅ Use selected billing month
        calendar.add(Calendar.DAY_OF_MONTH, 10);
        Date dueDate = calendar.getTime();

        // ✅ Create Bill Object
        Bill bill = new Bill();
        bill.setCustomer(customer);
        bill.setInvoiceId(UUID.randomUUID().toString());
        bill.setMonthDate(monthDate);
        bill.setTotalBillAmount(totalBillAmount);
        bill.setPaymentStatus(PaymentStatus.PENDING);
        bill.setCreatedAt(monthDate);  // ✅ Use `monthDate` instead of `new Date()`
        bill.setDueDate(dueDate);

        // ✅ Save bill
        Bill savedBill = billRepository.save(bill);

        // ✅ Reset unit consumption for the customer
        customer.setUnitConsumption(0);
        customerRepository.save(customer);

        // ✅ Logging for Debugging
        System.out.println("Bill Generated - Invoice ID: " + bill.getInvoiceId());
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
}
