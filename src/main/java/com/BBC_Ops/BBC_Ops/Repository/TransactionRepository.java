package com.BBC_Ops.BBC_Ops.Repository;
import com.BBC_Ops.BBC_Ops.Model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
