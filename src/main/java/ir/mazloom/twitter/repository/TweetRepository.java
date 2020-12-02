package ir.mazloom.twitter.repository;

import ir.mazloom.twitter.entity.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TweetRepository extends JpaRepository<Tweet, Long> {

    List<Tweet> findAllByUserId(Long id);
}
