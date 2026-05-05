package tn.enicarthage.qartnet.dto.request;

import lombok.Data;
import tn.enicarthage.qartnet.model.RepositoryAccessLevel;
import tn.enicarthage.qartnet.model.RepositoryAccessRole;

@Data
public class RepositoryAccessRequest {
    private String actorKey;
    private RepositoryAccessRole role = RepositoryAccessRole.COLLABORATOR;
    private RepositoryAccessLevel level = RepositoryAccessLevel.READ;
}
