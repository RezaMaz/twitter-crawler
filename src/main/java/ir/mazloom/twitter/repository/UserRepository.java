package ir.mazloom.twitter.repository;

import ir.mazloom.twitter.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllBySeedTrueAndCrawlingTrueAndFinishFalse();

    List<User> findAllBySeedTrueAndCrawlingFalseAndFinishFalse();

    List<User> findAllBySeedFalseAndCrawlingTrueAndFinishFalse();

    List<User> findAllBySeedFalseAndCrawlingFalseAndFinishFalse();

    List<User> findAllBySeedTrueAndCrawlingTrueAndTweetFinishFalse();

    List<User> findAllBySeedTrueAndCrawlingFalseAndTweetFinishFalse();

    List<User> findAllBySeedFalseAndCrawlingTrueAndTweetFinishFalse();

    List<User> findAllBySeedFalseAndCrawlingFalseAndTweetFinishFalse();

}
