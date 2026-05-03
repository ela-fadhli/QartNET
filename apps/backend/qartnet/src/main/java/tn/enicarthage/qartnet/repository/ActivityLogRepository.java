package tn.enicarthage.qartnet.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.enicarthage.qartnet.model.ActivityLog;
import tn.enicarthage.qartnet.shared.enums.ActivityType;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<ActivityLog> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime after);

    Page<ActivityLog> findByAction(ActivityType action, Pageable pageable);
}
