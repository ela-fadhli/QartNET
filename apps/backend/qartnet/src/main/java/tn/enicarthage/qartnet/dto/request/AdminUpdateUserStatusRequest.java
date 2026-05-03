package tn.enicarthage.qartnet.dto.request;

import jakarta.validation.constraints.NotNull;
import tn.enicarthage.qartnet.shared.enums.AccountStatus;

public record AdminUpdateUserStatusRequest(

        @NotNull(message = "Status is required")
        AccountStatus status
) {}
