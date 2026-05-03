package tn.enicarthage.qartnet.dto.request;

import jakarta.validation.constraints.NotNull;
import tn.enicarthage.qartnet.shared.enums.ReportStatus;

public record ResolveReportRequest(

        @NotNull(message = "Resolution status is required")
        ReportStatus status
) {}
