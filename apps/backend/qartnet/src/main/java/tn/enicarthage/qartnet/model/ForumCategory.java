package tn.enicarthage.qartnet.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "forum_categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ForumCategory extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forum_id", nullable = false)
    private Forum forum;
}
