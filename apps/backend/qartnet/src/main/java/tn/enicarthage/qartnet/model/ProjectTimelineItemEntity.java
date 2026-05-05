package tn.enicarthage.qartnet.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_timeline_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectTimelineItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @com.fasterxml.jackson.annotation.JsonProperty("date")
    private String date;
    
    @com.fasterxml.jackson.annotation.JsonProperty("title")
    private String title;

    @Column(length = 1000)
    private String description;

    private String status;
    private String icon;
    private String color;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectEntity project;
}
