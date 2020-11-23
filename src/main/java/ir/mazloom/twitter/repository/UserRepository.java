package ir.mazloom.twitter.repository;

import ir.mazloom.twitter.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllBySeedTrueAndCrawlingTrueAndFinishFalseOrderByRelationCountAsc();

    List<User> findAllBySeedTrueAndCrawlingFalseAndFinishFalseOrderByRelationCountAsc();

    List<User> findAllBySeedFalseAndCrawlingTrueAndFinishFalse();

    List<User> findAllBySeedFalseAndCrawlingFalseAndFinishFalse();

    List<User> findAllBySeedTrueAndCrawlingTrueAndTweetFinishFalse();

    List<User> findAllBySeedTrueAndCrawlingFalseAndTweetFinishFalse();

    List<User> findAllBySeedFalseAndCrawlingTrueAndTweetFinishFalse();

    List<User> findAllBySeedFalseAndCrawlingFalseAndTweetFinishFalse();

    List<User> findAllBySeedTrue();

    Optional<User> findByScreenName(String screenName);

}
