package ir.mazloom.twitter.repository;

import ir.mazloom.twitter.entity.Relationship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RelationshipRepository extends JpaRepository<Relationship, Long> {

    List<Relationship> findAllByFollowingId(Long id); //getFollowers

    List<Relationship> findAllByFollowerId(Long id); //getFollowings

}
