package com.vemeet.backend.repository

import com.vemeet.backend.model.Chat
import com.vemeet.backend.model.ChatAsset
import com.vemeet.backend.model.Message
import com.vemeet.backend.model.User
import io.lettuce.core.dynamic.annotation.Param
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository


import java.time.Instant

@Repository
interface ChatRepository : JpaRepository<Chat, Long> {
    fun findByUser1IdOrUser2Id(user1Id: Long, user2Id: Long): List<Chat>

    @Query("SELECT c FROM Chat c WHERE (c.user1 = :user1 AND c.user2 = :user2) OR (c.user1 = :user2 AND c.user2 = :user1)")
    fun findChatBetweenUsers(@Param("user1") user1: User, @Param("user2") user2: User): Chat?

}

@Repository
interface MessageRepository : JpaRepository<Message, Long> {
    fun findByChatIdOrderByCreatedAtDesc(chatId: Long): List<Message>
}

@Repository
interface ChatAssetRepository : JpaRepository<ChatAsset, Long> {
}