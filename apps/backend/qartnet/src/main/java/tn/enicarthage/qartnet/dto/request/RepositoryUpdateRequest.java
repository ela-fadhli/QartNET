package tn.enicarthage.qartnet.dto.request;

import lombok.Data;
import tn.enicarthage.qartnet.model.RepositoryVisibility;

@Data
public class RepositoryUpdateRequest {
    private String name;
    private String description;
    private RepositoryVisibility visibility;
}
