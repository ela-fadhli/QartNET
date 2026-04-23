package tn.enicarthage.qartnet.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "repository_contributors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryContributorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String initials;
    private String name;
    private String role;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "repository_id")
    private RepositoryEntity repository;
}