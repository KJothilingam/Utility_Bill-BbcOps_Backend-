package com.BBC_Ops.BBC_Ops.Repository;

import com.BBC_Ops.BBC_Ops.Model.BillingModifier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillingModifierRepository extends JpaRepository<BillingModifier, Long> {
    Optional<BillingModifier> findByModifierName(String modifierName);
}
