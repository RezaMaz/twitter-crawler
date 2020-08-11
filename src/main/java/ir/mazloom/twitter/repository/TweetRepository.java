package ir.mazloom.twitter.repository;

import ir.mazloom.twitter.entity.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TweetRepository extends JpaRepository<Tweet, Long> {

}
