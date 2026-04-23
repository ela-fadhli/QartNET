package tn.enicarthage.qartnet.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String initials;
    private String name;
    private String specialty;
    private String role;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectEntity project;
}