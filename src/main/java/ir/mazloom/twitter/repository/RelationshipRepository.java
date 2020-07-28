package ir.mazloom.twitter.repository;

import ir.mazloom.twitter.entity.Relationship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelationshipRepository extends JpaRepository<Relationship, Long> {
}
