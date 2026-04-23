package tn.enicarthage.qartnet.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String status;
    private String assigneeInitials;
    private String dueDate;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "phase_id")
    private ProjectPhaseEntity phase;
}