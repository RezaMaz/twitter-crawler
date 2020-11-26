package ir.mazloom.twitter;

import ir.mazloom.twitter.entity.Tweet;
import ir.mazloom.twitter.entity.User;
import ir.mazloom.twitter.repository.TweetRepository;
import ir.mazloom.twitter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
                            (user.getBiography() == null ? "\n" : user.getBiography().replaceAll("\n", " ").replaceAll("\r", " ") + "\n")
                    , Charsets.UTF_8, true);
        }
        System.out.println("twitter user report finished.");
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
        System.out.println("twitter tweet report finished.");
    }

    void downloadImage(Long userId) throws TwitterException, IOException, InterruptedException {
        try (InputStream in = new URL(twitter.showUser(userId).getProfileImageURL().replace("_normal", "")).openStream()) {
            File file = new File("C:\\download\\" + userId + ".png");
            FileUtils.copyInputStreamToFile(in, file);
            Thread.sleep(1000);
        }
    }

}
