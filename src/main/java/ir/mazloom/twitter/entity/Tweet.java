package ir.mazloom.twitter.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "TBL_TWEET")
public class Tweet {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "D_CREATED_AT")
    private Date createdAt;

    @Column(name = "C_TEXT")
    private String text;

    @Column(name = "N_IN_REPLY_TO_STATUS_ID")
    private Long inReplyToStatusId;

    @Column(name = "N_IN_REPLY_TO_USER_ID")
    private Long inReplyToUserId;

    @Column(name = "C_IN_REPLY_TO_SCREEN_NAME")
    private String inReplyToScreenName;

    @Column(name = "B_IS_TRUNCATED")
    private Boolean isTruncated;

    @Column(name = "B_IS_FAVORITED")
    private Boolean isFavorited;

    @Column(name = "B_IS_RETWEETED")
    private Boolean isRetweeted;

    @Column(name = "N_FAVORITE_COUNT")
    private Integer favoriteCount;

    @Column(name = "N_RETWEET_COUNT")
    private Integer retweetCount;

    @Column(name = "C_LANG")
    private String lang;

    @Setter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "F_USER_ID", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "TWEET2USER"))
    private User user;

    @Column(name = "F_USER_ID", nullable = false)
    private Long userId;

}
