package ir.mazloom.twitter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "twitter.api")
public class TwitterApiProperties {

    private List<String> consumerKey;
    private List<String> consumerSecret;
    private List<String> accessToken;
    private List<String> tokenSecret;

}
