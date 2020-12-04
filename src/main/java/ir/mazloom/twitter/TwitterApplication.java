package ir.mazloom.twitter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class TwitterApplication {

    @Autowired
    private TwitterApiProperties twitterApiProperties;

    public static void main(String[] args) {
        SpringApplication.run(TwitterApplication.class, args);
    }

    @Bean
    List<Twitter> createTwitterAPIs() {
        List<Twitter> twitters = new ArrayList<>();

        for (int i = 0; i < twitterApiProperties.getConsumerKey().size(); i++) {
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey(twitterApiProperties.getConsumerKey().get(i))
                    .setOAuthConsumerSecret(twitterApiProperties.getConsumerSecret().get(i))
                    .setOAuthAccessToken(twitterApiProperties.getAccessToken().get(i))
                    .setOAuthAccessTokenSecret(twitterApiProperties.getTokenSecret().get(i));
            TwitterFactory tf = new TwitterFactory(cb.build());
            twitters.add(tf.getInstance());
        }

        return twitters;
    }
}
