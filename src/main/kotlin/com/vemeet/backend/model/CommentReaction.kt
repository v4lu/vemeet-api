package com.vemeet.backend.model
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "comment_reactions")
data class CommentReaction(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = User(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    val comment: Comment = Comment(),

    @Column(name = "reaction_type", nullable = false)
    var reactionType: String = "",

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now()
)