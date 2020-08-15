package ir.mazloom.twitter;

import ir.mazloom.twitter.entity.Tweet;
import ir.mazloom.twitter.entity.User;
import ir.mazloom.twitter.repository.TweetRepository;
import ir.mazloom.twitter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

@RequiredArgsConstructor
@Component
public class ReportGenerator {

    private final UserRepository userRepository;
    private final TweetRepository tweetRepository;
    private final Twitter twitter;

    void twitterUserReport() throws IOException {
        File file = new File("twitter_user.txt");
        List<User> all = userRepository.findAll();
        for (User user : all) {
            FileUtils.writeStringToFile(file,
                    user.getId() + "\n" +
                            user.getFollowersCount() + "\n" +
                            user.getFriendsCount() + "\n" +
                            user.getScreenName() + "\n" +
                            user.getCreatedAt() + "\n" +
                            user.getBiography().replaceAll("\n", " ").replaceAll("\r", " ") + "\n"
                    , Charsets.UTF_8, true);
        }
    }

    void twitterTweetReport() throws IOException {
        File file = new File("twitter_tweet.txt");
        List<Tweet> all = tweetRepository.findAll();
        for (Tweet tweet : all) {
            FileUtils.writeStringToFile(file,
                    tweet.getId() + "\n" +
                            tweet.getUserId() + "\n" +
                            tweet.getFavoriteCount() + "\n" +
                            tweet.getRetweetCount() + "\n" +
                            tweet.getCreatedAt() + "\n" +
                            tweet.getInReplyToStatusId() + "\n" +
                            tweet.getInReplyToUserId() + "\n" +
                            tweet.getText().replaceAll("\n", " ").replaceAll("\r", " ") + "\n"
                    , Charsets.UTF_8, true);
        }
    }

    void screenNameToDatabaseUserRecord() throws IOException {
        File file = new File("screen_name_list.txt");
        FileUtils.readLines(file, Charset.defaultCharset()).forEach(q -> {
            try {
                ResponseList<Status> userTimeline = twitter.getUserTimeline(q);
                if (userTimeline.size() != 0) {
                    Status x = userTimeline.get(0);
                    User user = new User();
                    user.setId(x.getUser().getId());
                    user.setTweetFinish(false);
                    user.setPage(0);
                    user.setFinish(false);
                    user.setSeed(true);
                    user.setScreenName(x.getUser().getScreenName());
                    user.setCreatedAt(x.getUser().getCreatedAt());
                    user.setFollowersCount(x.getUser().getFollowersCount());
                    user.setFriendsCount(x.getUser().getFriendsCount());
                    user.setCrawling(false);
                    userRepository.saveAndFlush(user);
                    System.out.println(user.getScreenName());
                }
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        });
    }
}
