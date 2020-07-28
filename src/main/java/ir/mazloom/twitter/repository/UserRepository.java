package ir.mazloom.twitter.repository;

import ir.mazloom.twitter.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
