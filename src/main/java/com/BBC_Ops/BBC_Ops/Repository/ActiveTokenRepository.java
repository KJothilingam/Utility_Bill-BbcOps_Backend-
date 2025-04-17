package com.BBC_Ops.BBC_Ops.Repository;

import com.BBC_Ops.BBC_Ops.Model.ActiveToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActiveTokenRepository extends JpaRepository<ActiveToken, String> {
    ActiveToken findByEmail(String email);
}
