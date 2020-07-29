package ir.mazloom.twitter;

import ir.mazloom.twitter.entity.Relationship;
import ir.mazloom.twitter.entity.User;
import ir.mazloom.twitter.repository.RelationshipRepository;
import ir.mazloom.twitter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class Crawler {

    private final Twitter twitter;
    private final UserRepository userRepository;
    private final RelationshipRepository relationshipRepository;

    @PostConstruct
    void init() {
        while (true) {
            User user = fetchUserFromDatabase();
            if (user == null)
                break;
            userCrawler(user);
        }
    }

    private User fetchUserFromDatabase() {
        List<User> allBySeedTrueAAndCrawlingTrue = userRepository.findAllBySeedTrueAndCrawlingTrue();
        if (allBySeedTrueAAndCrawlingTrue.size() > 0)
            return allBySeedTrueAAndCrawlingTrue.get(0);
        else {
            List<User> allBySeedTrueAAndCrawlingFalse = userRepository.findAllBySeedTrueAndCrawlingFalse();
            if (allBySeedTrueAAndCrawlingFalse.size() > 0)
                return allBySeedTrueAAndCrawlingFalse.get(0);
            /*else {
                List<User> allBySeedFalseAAndCrawlingTrue = userRepository.findAllBySeedFalseAndCrawlingTrue();
                if (allBySeedFalseAAndCrawlingTrue.size() > 0)
                    return allBySeedFalseAAndCrawlingTrue.get(0);
                else {
                    List<User> allBySeedFalseAAndCrawlingFalse = userRepository.findAllBySeedFalseAndCrawlingFalse();
                    if (allBySeedFalseAAndCrawlingFalse.size() > 0)
                        return allBySeedFalseAAndCrawlingFalse.get(0);
                }
            }*/
        }
        return null;
    }

    private void userCrawler(User user) {
        try {
            log.info("start crawling user: " + user.getScreenName());

            persistUser(user, twitter.getUserTimeline(user.getScreenName()).get(0).getUser());
            user.setCrawling(true);

            persistFollowers(user);
            persistFollowings(user);

            setFinishStatus(user);

            log.info("finish crawling user: " + user.getScreenName());
        } catch (TwitterException e) {
            e.printStackTrace();
        }
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
        if (user.getId() != null && user.getFriendsCount() == null) //this user is existed in database with fake id
            userRepository.deleteById(user.getId());
        user.setBiography(twitterUser.getDescription());
        user.setCreatedAt(twitterUser.getCreatedAt());
        user.setFollowersCount(twitterUser.getFollowersCount());
        user.setFriendsCount(twitterUser.getFriendsCount());
        user.setId(twitterUser.getId());
        user.setScreenName(twitterUser.getScreenName());
        userRepository.saveAndFlush(user);
    }

    private void persistFollowers(User user) throws TwitterException {
        long cursor = user.getCursor();
        while (true) {
            PagableResponseList<twitter4j.User> followerList = twitter.getFollowersList(user.getScreenName(), cursor);

            followerList.forEach(follower -> {
                persistUser(new User(), follower);

                Relationship relationship = new Relationship();
                relationship.setFollowerId(follower.getId());
                relationship.setFollowingId(user.getId());
                relationshipRepository.saveAndFlush(relationship);
            });

            if (followerList.hasNext()) {
                cursor = followerList.getNextCursor();
                user.setCursor(cursor);
                userRepository.saveAndFlush(user);
            } else
                break;

            log.info("followersCount: " + user.getFollowersCount());
            log.info("followerCount until now: " + relationshipRepository.findAllByFollowingId(user.getId()).size());

            waitBetweenRequest();
        }
    }

    private void persistFollowings(User user) throws TwitterException {
        long cursor = user.getCursor();
        while (true) {
            PagableResponseList<twitter4j.User> followingList = twitter.getFriendsList(user.getScreenName(), cursor);

            followingList.forEach(following -> {
                persistUser(new User(), following);

                Relationship relationship = new Relationship();
                relationship.setFollowerId(user.getId());
                relationship.setFollowingId(following.getId());
                relationshipRepository.saveAndFlush(relationship);
            });

            if (followingList.hasNext()) {
                cursor = followingList.getNextCursor();
                user.setCursor(cursor);
                userRepository.saveAndFlush(user);
            } else
                break;

            log.info("followingCount: " + user.getFriendsCount());
            log.info("followingCount until now: " + relationshipRepository.findAllByFollowerId(user.getId()).size());

            waitBetweenRequest();
        }
    }

    // 15 Requests / 15-min window
    void waitBetweenRequest() {
        try {
            Thread.sleep(65 * 1000); //65 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
