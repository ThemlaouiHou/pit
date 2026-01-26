package com.pit.domain;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import lombok.Getter; import lombok.Setter; import lombok.NoArgsConstructor;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "places")
public class Place {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false) private String name;
    @Column(columnDefinition="text") private String description;
    @Column(nullable=false) private double lat;
    @Column(nullable=false) private double lng;
    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private PlaceStatus status = PlaceStatus.PENDING;
    @ManyToOne(optional = false)
@JoinColumn(name = "created_by")
private User createdBy;
@Column(name = "avg_rating", nullable = false)
private double avgRating = 0.0;
@Column(name = "ratings_count", nullable = false)
private int ratingsCount = 0;
@CreationTimestamp private Instant createdAt;
}
