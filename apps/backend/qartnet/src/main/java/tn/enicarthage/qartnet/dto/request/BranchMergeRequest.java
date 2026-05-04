package tn.enicarthage.qartnet.dto.request;

import lombok.Data;

@Data
public class BranchMergeRequest {
    private String sourceBranch;
    private String targetBranch;
}
