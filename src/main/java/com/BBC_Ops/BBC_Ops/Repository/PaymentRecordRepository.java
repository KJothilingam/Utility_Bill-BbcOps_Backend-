package com.BBC_Ops.BBC_Ops.Repository;

import com.BBC_Ops.BBC_Ops.Model.PaymentRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {
    // Find payment records by meter number
    List<PaymentRecord> findByMeterNumber(String meterNumber);

    @Query("SELECT SUM(p.finalAmountPaid) FROM PaymentRecord p WHERE FUNCTION('MONTH', p.paymentDate) = FUNCTION('MONTH', CURRENT_DATE) AND FUNCTION('YEAR', p.paymentDate) = FUNCTION('YEAR', CURRENT_DATE)")
    Double getTotalPaymentsForCurrentMonth();

    @Query("SELECT p FROM PaymentRecord p ORDER BY p.paymentDate DESC")
    Page<PaymentRecord> findLatestPayments(Pageable pageable);

    @Query("SELECT WEEK(p.paymentDate) AS weekNumber, SUM(p.totalBillAmount) AS totalAmount " +
            "FROM PaymentRecord p " +
            "WHERE YEAR(p.paymentDate) = YEAR(CURRENT_DATE) " +
            "GROUP BY WEEK(p.paymentDate) " +
            "ORDER BY weekNumber")
    List<Object[]> getWeeklyPayments();

}
