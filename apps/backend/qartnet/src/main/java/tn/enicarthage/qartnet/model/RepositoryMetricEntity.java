package tn.enicarthage.qartnet.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "repository_metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryMetricEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer totalCommits;
    private Integer totalBranches;
    private Integer totalContributors;
    private Integer totalFiles;

    @JsonBackReference
    @OneToOne
    @JoinColumn(name = "repository_id")
    private RepositoryEntity repository;
}
