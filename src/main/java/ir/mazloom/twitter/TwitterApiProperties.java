package ir.mazloom.twitter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "twitter.api")
public class TwitterApiProperties {
    private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String tokenSecret;
}
