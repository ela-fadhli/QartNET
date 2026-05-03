package tn.enicarthage.qartnet.dto.response;

import tn.enicarthage.qartnet.shared.enums.ReportStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ContentReportResponse(
        Long id,
        UUID reporterPublicId,
        String reporterUsername,
        String contentType,
        Long contentId,
        String reason,
        ReportStatus status,
        UUID resolvedByPublicId,
        String resolvedByUsername,
        LocalDateTime resolvedAt,
        LocalDateTime createdAt
) {}
