package com.donorconnect.billingservice.service;
import com.donorconnect.billingservice.entity.BillingRef;
import com.donorconnect.billingservice.repository.BillingRefRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service @RequiredArgsConstructor
public class BillingService {
    private final BillingRefRepository repo;
    public List<BillingRef> getAll() { return repo.findAll(); }
    public List<BillingRef> getByIssueId(Long issueId) { return repo.findByIssueId(issueId); }
    public BillingRef save(BillingRef ref) { return repo.save(ref); }
}
