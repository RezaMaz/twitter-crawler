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
import java.util.List;

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
        List<User> allBySeedTrueAAndCrawlingTrue = userRepository.findAllBySeedTrueAndCrawlingTrueAndFinishFalse();
        if (allBySeedTrueAAndCrawlingTrue.size() > 0)
            return allBySeedTrueAAndCrawlingTrue.get(0);
        else {
            List<User> allBySeedTrueAAndCrawlingFalse = userRepository.findAllBySeedTrueAndCrawlingFalseAndFinishFalse();
            if (allBySeedTrueAAndCrawlingFalse.size() > 0)
                return allBySeedTrueAAndCrawlingFalse.get(0);
            /*else {
                List<User> allBySeedFalseAAndCrawlingTrue = userRepository.findAllBySeedFalseAndCrawlingTrueAndFinishFalse();
                if (allBySeedFalseAAndCrawlingTrue.size() > 0)
                    return allBySeedFalseAAndCrawlingTrue.get(0);
                else {
                    List<User> allBySeedFalseAAndCrawlingFalse = userRepository.findAllBySeedFalseAndCrawlingFalseAndFinishFalse();
                    if (allBySeedFalseAAndCrawlingFalse.size() > 0)
                        return allBySeedFalseAAndCrawlingFalse.get(0);
                }
            }*/
        }
        return null;
    }

    private void userCrawler(User debUser) {
        try {
            log.info("start crawling user: " + debUser.getScreenName());

            User updatedUser = persistUser(twitter.getUserTimeline(debUser.getScreenName()).get(0).getUser());
            updatedUser.setCrawling(true);

            persistFollowers(updatedUser);
            persistFollowings(updatedUser);

            updatedUser.setCrawling(false);
            updatedUser.setFinish(true);
            userRepository.saveAndFlush(updatedUser);

            log.info("finish crawling user: " + updatedUser.getScreenName());
        } catch (TwitterException e) {
            try {
                log.error("TwitterException please wait(in seconds): " + e.getRateLimitStatus().getSecondsUntilReset());
                Thread.sleep(e.getRateLimitStatus().getSecondsUntilReset() * 1000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }
    }

    private User persistUser(twitter4j.User twitterUser) {
        User user;
        if (userRepository.existsById(twitterUser.getId())) {
            user = userRepository.findById(twitterUser.getId()).get();
        } else {
            user = new User();
            user.setSeed(false);
            user.setFinish(false);
            user.setCrawling(false);
            user.setFollowerCursor(-1L);
            user.setFollowingCursor(-1L);
        }
        user.setBiography(twitterUser.getDescription());
        user.setCreatedAt(twitterUser.getCreatedAt());
        user.setFollowersCount(twitterUser.getFollowersCount());
        user.setFriendsCount(twitterUser.getFriendsCount());
        user.setId(twitterUser.getId());
        user.setScreenName(twitterUser.getScreenName());
        return userRepository.saveAndFlush(user);
    }

    private void persistFollowers(User user) throws TwitterException {
        long cursor = user.getFollowerCursor();
        while (true) {
            PagableResponseList<twitter4j.User> followerList = twitter.getFollowersList(user.getScreenName(), cursor);

            followerList.forEach(follower -> {
                persistUser(follower);

                Relationship relationship = new Relationship();
                relationship.setFollowerId(follower.getId());
                relationship.setFollowingId(user.getId());
                relationshipRepository.saveAndFlush(relationship);
            });

            if (followerList.hasNext()) {
                user.setFollowerCursor(followerList.getNextCursor());
                userRepository.saveAndFlush(user);
            } else
                break;

            log.info("followersCount: " + user.getFollowersCount());
            log.info("followerCount until now: " + relationshipRepository.findAllByFollowingId(user.getId()).size());

            waitBetweenRequest();
        }
    }

    private void persistFollowings(User user) throws TwitterException {
        long cursor = user.getFollowingCursor();
        while (true) {
            PagableResponseList<twitter4j.User> followingList = twitter.getFriendsList(user.getScreenName(), cursor);

            followingList.forEach(following -> {
                persistUser(following);

                Relationship relationship = new Relationship();
                relationship.setFollowerId(user.getId());
                relationship.setFollowingId(following.getId());
                relationshipRepository.saveAndFlush(relationship);
            });

            if (followingList.hasNext()) {
                user.setFollowingCursor(followingList.getNextCursor());
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
