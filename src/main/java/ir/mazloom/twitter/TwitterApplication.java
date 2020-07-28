package ir.mazloom.twitter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

@SpringBootApplication
public class TwitterApplication {

    @Autowired
    private TwitterApiProperties twitterApiProperties;

    public static void main(String[] args) {
        SpringApplication.run(TwitterApplication.class, args);
    }

    @Bean
    Twitter createTwitter() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(twitterApiProperties.getConsumerKey())
                .setOAuthConsumerSecret(twitterApiProperties.getConsumerSecret())
                .setOAuthAccessToken(twitterApiProperties.getAccessToken())
                .setOAuthAccessTokenSecret(twitterApiProperties.getTokenSecret());
        TwitterFactory tf = new TwitterFactory(cb.build());
        return tf.getInstance();
    }
}
