package ir.mazloom.twitter;

import ir.mazloom.twitter.entity.Relationship;
import ir.mazloom.twitter.entity.Tweet;
import ir.mazloom.twitter.entity.TwitterUser;
import ir.mazloom.twitter.entity.User;
import ir.mazloom.twitter.repository.RelationshipRepository;
import ir.mazloom.twitter.repository.TweetRepository;
import ir.mazloom.twitter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import twitter4j.*;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class Crawler {

    private final Twitter twitter;
    private final UserRepository userRepository;
    private final TweetRepository tweetRepository;
    private final RelationshipRepository relationshipRepository;

    @PostConstruct
    void init() {
        while (true) {
            User user = null;
            try {
                user = fetchUserFromDatabase();
//                user = fetchUserFromDatabaseForCrawlingTweet();
                if (user == null)
                    break;
                relationCrawler(user);
//                tweetCrawler(user);
            } catch (TwitterException e) {
                if (e.getErrorMessage() != null && e.getErrorMessage().equals("Rate limit exceeded")) {
                    log.error("TwitterException please wait(in seconds): " + e.getRateLimitStatus().getSecondsUntilReset());
                    try {
                        Thread.sleep(e.getRateLimitStatus().getSecondsUntilReset() * 1000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                } else {
                    user.setFinish(true);
//                    user.setTweetFinish(true);
                    userRepository.saveAndFlush(user);
                }
            }
        }
    }

    private User fetchUserFromDatabase() {
        List<User> allBySeedTrueAAndCrawlingTrue = userRepository.findAllBySeedTrueAndCrawlingTrueAndFinishFalseOrderByRelationCountAsc();
        if (allBySeedTrueAAndCrawlingTrue.size() > 0)
            return allBySeedTrueAAndCrawlingTrue.get(0);
        else {
            List<User> allBySeedTrueAAndCrawlingFalse = userRepository.findAllBySeedTrueAndCrawlingFalseAndFinishFalseOrderByRelationCountAsc();
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

    private User fetchUserFromDatabaseForCrawlingTweet() {
        List<User> allBySeedTrueAAndCrawlingTrue = userRepository.findAllBySeedTrueAndCrawlingTrueAndTweetFinishFalse();
        if (allBySeedTrueAAndCrawlingTrue.size() > 0)
            return allBySeedTrueAAndCrawlingTrue.get(0);
        else {
            List<User> allBySeedTrueAAndCrawlingFalse = userRepository.findAllBySeedTrueAndCrawlingFalseAndTweetFinishFalse();
            if (allBySeedTrueAAndCrawlingFalse.size() > 0)
                return allBySeedTrueAAndCrawlingFalse.get(0);
            /*else {
                List<User> allBySeedFalseAAndCrawlingTrue = userRepository.findAllBySeedFalseAndCrawlingTrueAndTweetFinishFalse();
                if (allBySeedFalseAAndCrawlingTrue.size() > 0)
                    return allBySeedFalseAAndCrawlingTrue.get(0);
                else {
                    List<User> allBySeedFalseAAndCrawlingFalse = userRepository.findAllBySeedFalseAndCrawlingFalseAndTweetFinishFalse();
                    if (allBySeedFalseAAndCrawlingFalse.size() > 0)
                        return allBySeedFalseAAndCrawlingFalse.get(0);
                }
            }*/
        }
        return null;
    }

    private void relationCrawler(User dbUser) throws TwitterException {
        log.info("start crawling user: " + dbUser.getId());

        User updatedUser = persistUser(twitter.showUser(dbUser.getId()));
        updatedUser.setCrawling(true);
        userRepository.saveAndFlush(updatedUser);

//        persistTwintFollowers(updatedUser);
        persistTwintFollowings(updatedUser);

        updatedUser.setCrawling(false);
        updatedUser.setFinish(true);
        userRepository.saveAndFlush(updatedUser);

        log.info("finish crawling user: " + updatedUser.getId());
    }

    private void tweetCrawler(User dbUser) throws TwitterException {
        log.info("start crawling user: " + dbUser.getId());

        dbUser.setCrawling(true);
        userRepository.saveAndFlush(dbUser);

        persistTweets(dbUser);

        dbUser.setCrawling(false);
        dbUser.setTweetFinish(true);
        userRepository.saveAndFlush(dbUser);

        log.info("finish crawling user: " + dbUser.getId());
    }

    private void persistTweets(User dbUser) throws TwitterException {
        Paging paging = new Paging();
        int page = dbUser.getPage();
        ResponseList<Status> userTimeline;
        do {
            page = page + 1;
            paging.setPage(page);
            dbUser.setPage(page);

            log.info("start crawling page:" + page);

            userTimeline = twitter.getUserTimeline(dbUser.getScreenName(), paging);
            userTimeline.forEach(q -> {
                Tweet tweet = new Tweet();
                tweet.setId(q.getId());
                tweet.setCreatedAt(q.getCreatedAt());
                tweet.setFavoriteCount(q.getFavoriteCount());
                tweet.setRetweetCount(q.getRetweetCount());
                tweet.setInReplyToScreenName(q.getInReplyToScreenName());
                tweet.setInReplyToStatusId(q.getInReplyToStatusId());
                tweet.setInReplyToUserId(q.getInReplyToUserId());
                tweet.setIsFavorited(q.isFavorited());
                tweet.setIsRetweeted(q.isRetweeted());
                tweet.setIsTruncated(q.isTruncated());
                tweet.setLang(q.getLang());
                tweet.setText(q.getText());
                tweet.setUserId(q.getUser().getId());
                tweetRepository.save(tweet);
            });
            tweetRepository.flush();
            userRepository.saveAndFlush(dbUser);

            log.info("finish crawling page:" + page);

        } while (userTimeline.size() > 0);
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
        try {
            user.setFollowersCount(twitterUser.getFollowersCount());
        } catch (NullPointerException e) {
            user.setFollowersCount(0);
        }
        try {
            user.setFriendsCount(twitterUser.getFriendsCount());
        } catch (NullPointerException e) {
            user.setFriendsCount(0);
        }
        user.setId(twitterUser.getId());
        user.setScreenName(twitterUser.getScreenName());
        return userRepository.saveAndFlush(user);
    }

    private void persistFollowers(User user) throws TwitterException {
        while (true) {
            PagableResponseList<twitter4j.User> followerList = twitter.getFollowersList(user.getScreenName(), user.getFollowerCursor());

            followerList.forEach(follower -> {
                persistUser(follower);

                Relationship relationship = new Relationship();
                relationship.setFollowerId(follower.getId());
                relationship.setFollowingId(user.getId());
                if (!relationshipRepository.existsRelationshipByFollowerIdAndFollowingId(follower.getId(), user.getId()))
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
        while (true) {
            PagableResponseList<twitter4j.User> followingList = twitter.getFriendsList(user.getScreenName(), user.getFollowingCursor());

            followingList.forEach(following -> {
                persistUser(following);

                Relationship relationship = new Relationship();
                relationship.setFollowerId(user.getId());
                relationship.setFollowingId(following.getId());
                if (!relationshipRepository.existsRelationshipByFollowerIdAndFollowingId(user.getId(), following.getId()))
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

    private void persistTwintFollowers(User user) {
        List<twitter4j.User> followerList = getTwintRelation(user.getScreenName(), "followers");
        followerList.forEach(follower -> {
            persistUser(follower);

            Relationship relationship = new Relationship();
            relationship.setFollowerId(follower.getId());
            relationship.setFollowingId(user.getId());
            if (!relationshipRepository.existsRelationshipByFollowerIdAndFollowingId(follower.getId(), user.getId()))
                relationshipRepository.saveAndFlush(relationship);
        });

        log.info("followersCount: " + user.getFollowersCount());
        log.info("followerCount until now: " + relationshipRepository.findAllByFollowingId(user.getId()).size());
        if (relationshipRepository.findAllByFollowingId(user.getId()).size() == 0)
            System.out.println("followerCount not gathered: " + user.getId());
    }

    private void persistTwintFollowings(User user) {
        List<twitter4j.User> followingList = getTwintRelation(user.getScreenName(), "following");

        followingList.forEach(following -> {
            persistUser(following);

            Relationship relationship = new Relationship();
            relationship.setFollowerId(user.getId());
            relationship.setFollowingId(following.getId());
            if (!relationshipRepository.existsRelationshipByFollowerIdAndFollowingId(user.getId(), following.getId()))
                relationshipRepository.saveAndFlush(relationship);
        });

        log.info("followingCount: " + user.getFriendsCount());
        log.info("followingCount until now: " + relationshipRepository.findAllByFollowerId(user.getId()).size());
        if (relationshipRepository.findAllByFollowerId(user.getId()).size() == 0)
            System.out.println("followingCount not gathered: " + user.getId());
    }

    // 15 Requests / 15-min window
    void waitBetweenRequest() {
        try {
            Thread.sleep(65 * 1000); //65 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<twitter4j.User> getTwintRelation(String username, String relation) {
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", "twint -u " + username + " --" + relation);
        builder.redirectErrorStream(true);
        Process p = null;
        try {
            p = builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String screenName;
        List<twitter4j.User> users = new ArrayList<>();
        while (true) {
            try {
                screenName = r.readLine();
                if (screenName == null) {
                    break;
                }
                if (screenName.contains("CRITICAL:root:twint"))
                    continue;
                try {
                    Optional<User> byScreenName = userRepository.findByScreenName(screenName);
                    if (byScreenName.isPresent()) {
                        TwitterUser twitterUser = new TwitterUser();
                        twitterUser.setId(byScreenName.get().getId());
                        twitterUser.setBiography(byScreenName.get().getBiography());
                        twitterUser.setCreatedAt(byScreenName.get().getCreatedAt());
                        twitterUser.setFollowersCount(byScreenName.get().getFollowersCount());
                        twitterUser.setFriendsCount(byScreenName.get().getFriendsCount());
                        twitterUser.setName(byScreenName.get().getName());
                        twitterUser.setScreenName(byScreenName.get().getScreenName());
                        users.add(twitterUser);
                    } else {
                        twitter4j.User user = twitter.showUser(screenName);
                        users.add(user);
                    }
                } catch (TwitterException e) {
                    log.error("TwitterException please wait(in seconds): " + e.getRateLimitStatus().getSecondsUntilReset());
                    try {
                        Thread.sleep(e.getRateLimitStatus().getSecondsUntilReset() * 1000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return users;
    }
}
