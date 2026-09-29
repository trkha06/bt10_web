package vn.hcmute.webpr.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.hcmute.webpr.jwt.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
