package com.BBC_Ops.BBC_Ops.Repository;

import com.BBC_Ops.BBC_Ops.Enum.PaymentStatus;
import com.BBC_Ops.BBC_Ops.Model.Bill;
import com.BBC_Ops.BBC_Ops.Model.Customer;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Date;
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

    List<Bill> findAll();

    // ✅ Fetch unpaid bills (both PENDING & OVERDUE)
    List<Bill> findByCustomer_MeterNumberAndPaymentStatusIn(String meterNumber, List<PaymentStatus> statuses);

    @Query(value = "SELECT b FROM Bill b WHERE b.paymentStatus = 'PENDING' ORDER BY b.dueDate DESC")
    List<Bill> findTopRecentPendingBills();  // Fetch all pending bills, limit applied in service

    @Query("SELECT COUNT(b) FROM Bill b WHERE b.paymentStatus = 'PENDING' OR b.paymentStatus = 'OVERDUE'")
    long countPendingAndOverdueBills();

    @Query("SELECT b FROM Bill b WHERE b.paymentStatus = 'OVERDUE' ORDER BY b.dueDate ASC")
    List<Bill> findOverdueBills();

    @Query("SELECT COUNT(b) FROM Bill b WHERE b.paymentStatus = 'PENDING'")
    long countPendingPayments();

    @Query("SELECT COUNT(b) FROM Bill b WHERE b.paymentStatus = 'PAID'")
    long countPaidPayments();

    @Query("SELECT COUNT(b) FROM Bill b WHERE b.paymentStatus = 'OVERDUE'")
    long countOverduePayments();

    @Query("SELECT FUNCTION('DATE_FORMAT', b.monthDate, '%b') AS month, SUM(b.totalBillAmount) " +
            "FROM Bill b " +
            "GROUP BY FUNCTION('DATE_FORMAT', b.monthDate, '%b') " +
            "ORDER BY MIN(b.monthDate)")
    List<Object[]> getMonthlyPayments();


    @Query("SELECT MONTH(b.monthDate) as month, SUM(b.totalBillAmount) as totalPayment, SUM(b.unitConsumed) as totalUnits FROM Bill b WHERE b.customer.customerId = :customerId GROUP BY MONTH(b.monthDate) ORDER BY MONTH(b.monthDate)")
    List<Object[]> getMonthlyStatsByCustomer(Long customerId);

    @Query("SELECT COUNT(b) > 0 FROM Bill b WHERE b.customer = :customer AND FUNCTION('YEAR', b.monthDate) = FUNCTION('YEAR', :monthDate) AND FUNCTION('MONTH', b.monthDate) = FUNCTION('MONTH', :monthDate)")
    boolean existsByCustomerAndMonth(@Param("customer") Customer customer, @Param("monthDate") Date monthDate);

}
