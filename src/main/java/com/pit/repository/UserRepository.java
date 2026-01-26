package com.pit.repository;
import com.pit.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
// Application component.
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
