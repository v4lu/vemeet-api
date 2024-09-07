package com.vemeet.backend.model

import jakarta.persistence.*
import org.hibernate.annotations.Type
import java.time.Duration
import java.time.Instant
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import com.vladmihalcea.hibernate.type.json.JsonType

@Entity
@Table(name = "recipes")
data class Recipe(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = User(),

    @Column(nullable = false)
    var title: String = "",

    @Type(JsonBinaryType::class)
    @Column(columnDefinition = "jsonb")
    var content: Map<String, Any>? = null,

    @Column(nullable = false, columnDefinition = "text")
    var instructions: String = "",

    @Type(JsonType::class)
    @Column(columnDefinition = "text[]", nullable = false)
    var ingredients: List<String> = emptyList(),

    @Column(name = "preparation_time")
    var preparationTime: Duration = Duration.ofMinutes(0),

    @Column(name = "cooking_time")
    var cookingTime: Duration = Duration.ofMinutes(0),

    var servings: Int = 0,

    @Enumerated(EnumType.STRING)
    var difficulty: Difficulty = Difficulty.MEDIUM,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: RecipeCategory? = null,

    @OneToMany(mappedBy = "recipe", cascade = [CascadeType.ALL], orphanRemoval = true)
    var images: MutableList<RecipeImage> = mutableListOf(),

    @ManyToMany
    @JoinTable(
        name = "recipe_tags",
        joinColumns = [JoinColumn(name = "recipe_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    var tags: MutableSet<Tag> = mutableSetOf(),

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now()
)

enum class Difficulty {
    EASY, MEDIUM, HARD
}