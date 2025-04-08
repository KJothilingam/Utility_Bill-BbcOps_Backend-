package com.BBC_Ops.BBC_Ops.Repository;

import com.BBC_Ops.BBC_Ops.Model.ReportRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRequestRepository extends JpaRepository<ReportRequest, Long> {
    List<ReportRequest> findByCustomerId(Long customerId);
}
