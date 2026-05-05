package tn.enicarthage.qartnet.dto.request;

import lombok.Data;

@Data
public class BranchCreateRequest {
    private String name;
    private String fromBranch;
}
