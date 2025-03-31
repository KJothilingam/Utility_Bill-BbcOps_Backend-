package com.BBC_Ops.BBC_Ops.Repository;

import com.BBC_Ops.BBC_Ops.Model.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentRecord, Long> {
}
