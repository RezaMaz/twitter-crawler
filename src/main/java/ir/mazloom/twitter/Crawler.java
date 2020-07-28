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
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Configuration
public class Crawler {

    private final Twitter twitter;
    private final UserRepository userRepository;
    private final RelationshipRepository relationshipRepository;

    @PostConstruct
    void init() {
        userCrawler(userRepository.findAllBySeedTrueAAndCrawlingTrue());
        userCrawler(userRepository.findAllBySeedTrueAAndCrawlingFalse());
        userCrawler(userRepository.findAllBySeedFalseAAndCrawlingTrue());
        userCrawler(userRepository.findAllBySeedFalseAAndCrawlingFalse());
    }

    private void userCrawler(List<User> userList) {
        userList.forEach(user -> {
            try {
                persistUser(user, twitter.getUserTimeline(user.getScreenName()).get(0).getUser());
                user.setCrawling(true);

                persistFollowers(user);
                persistFollowings(user);

                setFinishStatus(user);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        });
    }

    private void setFinishStatus(User user) {
        Optional<User> byId = userRepository.findById(user.getId());
        User foundUser = byId.orElseThrow(EntityNotFoundException::new);

        if (relationshipRepository.findAllByFollowingId(user.getId()).size() == foundUser.getFollowersCount() &&
                relationshipRepository.findAllByFollowerId(user.getId()).size() == foundUser.getFriendsCount()) {
            user.setCrawling(false);
            user.setFinish(true);
        } else {
            user.setCrawling(true);
            user.setFinish(false);
        }
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

    private void persistFollowers(User user) throws TwitterException {
        long cursor = -1L;
        while (true) {
            PagableResponseList<twitter4j.User> followerList = twitter.getFollowersList(user.getScreenName(), cursor);
            followerList.forEach(follower -> {
                persistUser(new User(), follower);

                Relationship relationship = new Relationship();
                relationship.setFollowerId(follower.getId());
                relationship.setFollowingId(user.getId());
                relationshipRepository.saveAndFlush(relationship);
            });
            cursor = followerList.getNextCursor();
            if (!followerList.hasNext()) {
                break;
            }
        }
    }

    private void persistFollowings(User user) throws TwitterException {
        long cursor = -1L;
        while (true) {
            PagableResponseList<twitter4j.User> followingList = twitter.getFriendsList(user.getScreenName(), cursor);
            followingList.forEach(following -> {
                persistUser(new User(), following);

                Relationship relationship = new Relationship();
                relationship.setFollowerId(user.getId());
                relationship.setFollowingId(following.getId());
                relationshipRepository.saveAndFlush(relationship);
            });
            cursor = followingList.getNextCursor();
            if (!followingList.hasNext()) {
                break;
            }
        }
    }

}
