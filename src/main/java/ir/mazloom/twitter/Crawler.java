package ir.mazloom.twitter;

import ir.mazloom.twitter.entity.Relationship;
import ir.mazloom.twitter.entity.User;
import ir.mazloom.twitter.repository.RelationshipRepository;
import ir.mazloom.twitter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
@Configuration
public class Crawler {

    private final Twitter twitter;
    private final UserRepository userRepository;
    private final RelationshipRepository relationshipRepository;

    @PostConstruct
    void init() {
        userRepository.findAll().forEach(user -> {
            try {
                getUserInformation(user);
                getRelationship(user);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        });
    }

    private void getUserInformation(User user) throws TwitterException {
        twitter4j.User twitterUser = twitter.getUserTimeline(user.getScreenName()).get(0).getUser();
        persistUser(user, twitterUser);
    }

    private void persistUser(User user, twitter4j.User twitterUser) {
        user.setBiography(twitterUser.getDescription());
        user.setCreatedAt(twitterUser.getCreatedAt());
        user.setFollowersCount(twitterUser.getFollowersCount());
        user.setFriendsCount(twitterUser.getFriendsCount());
        user.setId(twitterUser.getId());
        user.setScreenName(twitterUser.getScreenName());
        userRepository.saveAndFlush(user);
    }

    private void getRelationship(User user) throws TwitterException {
        long cursor = -1L;
        while (true) {
            PagableResponseList<twitter4j.User> followersList = twitter.getFollowersList(user.getScreenName(), cursor);
            followersList.forEach(x -> {
                User follower = new User();
                persistUser(follower, x);

                Relationship relationship = new Relationship();
                relationship.setFollowerId(follower.getId());
                relationship.setFollowingId(user.getId());
                relationshipRepository.saveAndFlush(relationship);
            });
            cursor = followersList.getNextCursor();
            if (!followersList.hasNext())
                break;
        }
    }

}
