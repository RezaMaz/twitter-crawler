package ir.mazloom.twitter;

import ir.mazloom.twitter.entity.User;
import ir.mazloom.twitter.repository.TweetRepository;
import ir.mazloom.twitter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

@RequiredArgsConstructor
@Component
public class ReportGenerator {

    private final UserRepository userRepository;
    private final TweetRepository tweetRepository;
    private final List<Twitter> twitterAPIs;

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

    @PostConstruct
    void twitterTweetReport() {
        File file = new File("sutfinal.csv");
        List<String> strings = null;
        try {
            strings = FileUtils.readLines(file, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        strings.forEach(x -> {
            try (InputStream in = new URL(getFreeTwitterApi().showUser(x.split(",")[2].replaceAll("\"","")).getProfileImageURL().replace("_normal", "")).openStream()) {
                File file1 = new File("C:\\download\\" + x.split(",")[2].replaceAll("\"","") + ".png");
                FileUtils.copyInputStreamToFile(in, file1);
            } catch (TwitterException e) {
                if (((TwitterException) e).getErrorMessage().equals("User not found."))
                    System.out.println(x.split(",")[2].replaceAll("\"",""));
            } catch (IOException e){}
        });

    }

    //        @PostConstruct
    void downloadImage() {
        Twitter freeTwitterApi = getFreeTwitterApi();
        List<User> allBySeedTrue = userRepository.findAllBySeedTrue();
        for (User user : allBySeedTrue) {
            try {
                try (InputStream in = new URL(freeTwitterApi.showUser(user.getId()).getProfileImageURL().replace("_normal", "")).openStream()) {
                    File file = new File("C:\\download\\" + user.getId() + ".png");
                    FileUtils.copyInputStreamToFile(in, file);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TwitterException e) {
                if (e.getErrorMessage() != null && e.getErrorMessage().equals("Rate limit exceeded")) {
                    freeTwitterApi = getFreeTwitterApi();
                }
            }
        }
    }

    private Twitter getFreeTwitterApi() {
        for (int i = 0; i < twitterAPIs.size(); i++) {
            try {
                twitterAPIs.get(i).showUser("jzarif");
                return twitterAPIs.get(i);
            } catch (TwitterException ignored) {
            }
        }
        try {
            Thread.sleep(1000 * 1000);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
        return twitterAPIs.get(0);
    }

}
