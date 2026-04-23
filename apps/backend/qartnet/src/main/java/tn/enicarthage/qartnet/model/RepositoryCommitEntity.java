package tn.enicarthage.qartnet.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "repository_commits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryCommitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hash;
    private String message;
    private String author;
    private String date;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "repository_id")
    private RepositoryEntity repository;
}