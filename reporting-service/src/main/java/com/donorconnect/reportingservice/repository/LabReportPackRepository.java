package com.donorconnect.reportingservice.repository;
import com.donorconnect.reportingservice.entity.LabReportPack;
import com.donorconnect.reportingservice.enums.ReportScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LabReportPackRepository extends JpaRepository<LabReportPack, Long> {
    List<LabReportPack> findByScope(ReportScope scope);
}
