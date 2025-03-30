package com.BBC_Ops.BBC_Ops.Repository;

import com.BBC_Ops.BBC_Ops.Enum.PaymentStatus;
import com.BBC_Ops.BBC_Ops.Model.Bill;
import com.BBC_Ops.BBC_Ops.Model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    // Find bill by invoice ID (unique identifier for each bill)
    Optional<Bill> findByInvoiceId(String invoiceId);

    // Find all bills for a specific customer
    List<Bill> findByCustomer(Customer customer);

    // Find all pending bills for a customer
    List<Bill> findByCustomerAndPaymentStatus(Customer customer, PaymentStatus status);

    // Find all unpaid bills (overdue or pending)
    List<Bill> findByPaymentStatusIn(List<PaymentStatus> statuses);

    List<Bill> findAll();

    boolean existsByCustomerAndMonthDate(Customer customer, Date monthDate);

}
