package com.vemeet.backend.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "post_images")
data class PostImage(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    val post: Post = Post(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    val image: Image = Image(),

    @Column(name = "order_index")
    var orderIndex: Int = 0,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now()
)