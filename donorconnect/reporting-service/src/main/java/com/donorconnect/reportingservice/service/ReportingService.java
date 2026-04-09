package com.donorconnect.reportingservice.service;
import com.donorconnect.reportingservice.entity.LabReportPack;
import com.donorconnect.reportingservice.enums.ReportScope;
import com.donorconnect.reportingservice.repository.LabReportPackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service @RequiredArgsConstructor
public class ReportingService {
    private final LabReportPackRepository repo;
    public List<LabReportPack> getAll() { return repo.findAll(); }
    public List<LabReportPack> getByScope(ReportScope scope) { return repo.findByScope(scope); }
    public LabReportPack save(LabReportPack pack) { return repo.save(pack); }
}
