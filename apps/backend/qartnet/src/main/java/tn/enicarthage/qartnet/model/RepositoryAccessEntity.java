package tn.enicarthage.qartnet.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "repository_access", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"repository_id", "actorKey"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryAccessEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String actorKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepositoryAccessRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepositoryAccessLevel level;

    @JsonBackReference
    @ManyToOne(optional = false)
    @JoinColumn(name = "repository_id", nullable = false)
    private RepositoryEntity repository;
}
