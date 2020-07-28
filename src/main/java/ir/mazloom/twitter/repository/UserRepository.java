package ir.mazloom.twitter.repository;

import ir.mazloom.twitter.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllBySeedTrueAAndCrawlingTrue();

    List<User> findAllBySeedTrueAAndCrawlingFalse();

    List<User> findAllBySeedFalseAAndCrawlingTrue();

    List<User> findAllBySeedFalseAAndCrawlingFalse();

}
