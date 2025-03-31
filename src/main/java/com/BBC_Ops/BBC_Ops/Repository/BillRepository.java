package com.BBC_Ops.BBC_Ops.Repository;

import com.BBC_Ops.BBC_Ops.Enum.PaymentStatus;
import com.BBC_Ops.BBC_Ops.Model.Bill;
import com.BBC_Ops.BBC_Ops.Model.Customer;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    @Modifying
    @Transactional
    @Query("UPDATE Bill b SET b.paymentStatus = 'OVERDUE' WHERE b.dueDate < CURRENT_DATE AND b.paymentStatus = 'PENDING'")
    void updateOverdueBills();
    List<Bill> findByCustomer_CustomerId(Long customerId);

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

    List<Bill> findByCustomer_MeterNumberAndPaymentStatus(String meterNumber, PaymentStatus paymentStatus);


    @Query(value = "SELECT b FROM Bill b WHERE b.paymentStatus = 'PENDING' ORDER BY b.dueDate DESC")
    List<Bill> findTopRecentPendingBills();  // Fetch all pending bills, limit applied in service

}
