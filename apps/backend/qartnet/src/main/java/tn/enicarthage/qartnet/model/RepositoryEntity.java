package tn.enicarthage.qartnet.model;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "repositories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    private String ownerDisplayName;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepositoryVisibility visibility;

    private String language;
    private Integer stars;
    private Integer forks;
    private Integer watchers;
    private String updatedAt;
    private String readmeTitle;
    private String readmeSubtitle;
    private String cloneUrl;

    //  BRANCHES
    @JsonManagedReference
    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepositoryBranchEntity> branches = new ArrayList<>();

    //  COMMITS
    @JsonManagedReference
    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepositoryCommitEntity> commits = new ArrayList<>();

    //  CONTRIBUTORS
    @JsonManagedReference
    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepositoryContributorEntity> contributors = new ArrayList<>();

    //FILES
    @JsonManagedReference
    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepositoryFileEntity> files = new ArrayList<>();

    //METRICS
    @JsonManagedReference
    @OneToOne(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true)
    private RepositoryMetricEntity metrics;
}