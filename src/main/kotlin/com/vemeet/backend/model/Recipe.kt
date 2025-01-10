package com.vemeet.backend.model

import com.fasterxml.jackson.databind.JsonNode
import jakarta.persistence.*
import org.hibernate.annotations.Type
import java.time.Duration
import java.time.Instant
import com.vladmihalcea.hibernate.type.json.JsonBinaryType

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

    @OneToMany(mappedBy = "recipe", cascade = [CascadeType.ALL], orphanRemoval = true)
    var ingredients: MutableList<Ingredient> = mutableListOf(),

    @Type(JsonBinaryType::class)
    @Column(columnDefinition = "jsonb")
    var content: JsonNode? = null,

    @Column(name = "preparation_time")
    var preparationTime: Duration = Duration.ofMinutes(0),

    @Column(name = "cooking_time")
    var cookingTime: Duration = Duration.ofMinutes(0),

    var servings: Int = 0,

    var difficulty: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: RecipeCategory? = null,

    @OneToMany(mappedBy = "recipe", cascade = [CascadeType.ALL], orphanRemoval = true)
    var images: MutableList<RecipeImage> = mutableListOf(),

    @OneToMany(mappedBy = "recipe", cascade = [CascadeType.ALL], orphanRemoval = true)
    var comments: MutableList<Comment> = mutableListOf(),

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
) {
    @Transient
    var reactions: List<Reaction> = listOf()
}


@Entity
@Table(name = "ingredients")
data class Ingredient(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String = "",

    @ManyToOne
    @JoinColumn(name = "recipe_id", nullable = false)
    var recipe: Recipe = Recipe(),
)