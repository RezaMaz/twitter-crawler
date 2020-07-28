package ir.mazloom.twitter.entity;

import lombok.*;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "RELATIONSHIP")
public class Relationship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Setter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "F_FOLLOWER_ID", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "FOLLOWER2USER"))
    private User follower;

    @Column(name = "F_FOLLOWER_ID")
    private Long followerId;

    @Setter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "F_FOLLOWING_ID", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "FOLLOWING2USER"))
    private User following;

    @Column(name = "F_FOLLOWING_ID")
    private Long followingId;

}
