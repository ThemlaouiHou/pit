package com.pit.domain;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;


import lombok.Getter; import lombok.Setter; import lombok.NoArgsConstructor;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true) private String email;
    @Column(nullable=false) private String password;
    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private Role role = Role.USER;
    @CreationTimestamp private Instant createdAt;
}
