package tn.enicarthage.qartnet.dto.request;

import jakarta.validation.constraints.NotEmpty;
import tn.enicarthage.qartnet.shared.enums.Role;

import java.util.Set;

public record AdminUpdateUserRolesRequest(

        @NotEmpty(message = "At least one role is required")
        Set<Role> roles
) {}
