package com.BBC_Ops.BBC_Ops.Repository;

import com.BBC_Ops.BBC_Ops.Model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByMeterNumber(String meterNumber);

    Optional<Customer> findByMeterNumber(String meterNumber);

    Customer findByEmail(String email);

    @Query("SELECT COUNT(c) FROM Customer c")
    long countTotalCustomers();

}
