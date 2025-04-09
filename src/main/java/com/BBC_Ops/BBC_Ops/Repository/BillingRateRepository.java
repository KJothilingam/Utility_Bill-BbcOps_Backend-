package com.BBC_Ops.BBC_Ops.Repository;

import com.BBC_Ops.BBC_Ops.Model.BillingRate;
import com.BBC_Ops.BBC_Ops.Model.ConnectionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillingRateRepository extends JpaRepository<BillingRate, Long> {
    Optional<BillingRate> findByConnectionType(ConnectionType connectionType);
}
