package tn.enicarthage.qartnet.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.enicarthage.qartnet.model.ContentReport;
import tn.enicarthage.qartnet.shared.enums.ReportStatus;

public interface ContentReportRepository extends JpaRepository<ContentReport, Long> {

    Page<ContentReport> findByStatus(ReportStatus status, Pageable pageable);

    Page<ContentReport> findAll(Pageable pageable);

    long countByStatus(ReportStatus status);
}
