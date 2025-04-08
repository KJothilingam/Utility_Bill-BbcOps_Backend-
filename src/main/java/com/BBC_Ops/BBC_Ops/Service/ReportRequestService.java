package com.BBC_Ops.BBC_Ops.Service;

import com.BBC_Ops.BBC_Ops.Enum.RequestStatus;
import com.BBC_Ops.BBC_Ops.Model.ReportRequest;
import com.BBC_Ops.BBC_Ops.Repository.ReportRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ReportRequestService {

    @Autowired
    private ReportRequestRepository repository;


    public ReportRequestService(ReportRequestRepository repository) {
        this.repository = repository;
    }

    public ReportRequest createRequest(ReportRequest request) {
        return repository.save(request);
    }

    public List<ReportRequest> getRequestsByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId);
    }

    public List<ReportRequest> getAllRequests() {
        return repository.findAll();
    }

    public void updateStatus(Long id, String status) {
        ReportRequest request = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Request not found"));

        try {
            RequestStatus newStatus = RequestStatus.valueOf(status.toUpperCase());
            request.setStatus(newStatus);
            repository.save(request);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value");
        }
    }
}
