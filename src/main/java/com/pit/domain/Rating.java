package com.pit.domain;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import lombok.Getter; import lombok.Setter; import lombok.NoArgsConstructor;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "ratings",
        uniqueConstraints = @UniqueConstraint(name="uk_rating_user_place", columnNames = {"user_id","place_id"}))
public class Rating {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional=false) private Place place;
    @ManyToOne(optional=false) private User user;
    @Column(nullable=false) private int score; // Stored in the 1..5 range.
    @Column(columnDefinition="text") private String comment;
    @CreationTimestamp private Instant createdAt;
}
