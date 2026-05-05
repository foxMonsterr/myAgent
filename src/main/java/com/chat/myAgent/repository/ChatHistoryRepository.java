package com.chat.myAgent.repository;

import com.chat.myAgent.model.entity.ChatHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistoryEntity, Long> {

    /**
     * 按会话ID查询历史
     */
    List<ChatHistoryEntity> findByConversationIdOrderByCreatedAtAsc(String conversationId);

    /**
     * 按用户名查询历史（分页）
     */
    Page<ChatHistoryEntity> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    /**
     * 查询用户的所有会话ID（去重）
     */
    @Query("SELECT DISTINCT c.conversationId FROM ChatHistoryEntity c WHERE c.username = :username ORDER BY c.conversationId")
    List<String> findDistinctConversationIdsByUsername(String username);

    /**
     * 统计用户的总对话轮数
     */
    long countByUsername(String username);

    /**
     * 统计指定时间范围内的对话数
     */
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 统计总 Token 消耗
     */
    @Query("SELECT COALESCE(SUM(c.promptTokens), 0) + COALESCE(SUM(c.completionTokens), 0) FROM ChatHistoryEntity c")
    Long sumTotalTokens();

    @Query("SELECT COALESCE(SUM(c.promptTokens), 0) FROM ChatHistoryEntity c")
    Long sumPromptTokens();

    @Query("SELECT COALESCE(SUM(c.completionTokens), 0) FROM ChatHistoryEntity c")
    Long sumCompletionTokens();

    /**
     * 删除指定会话的所有记录
     */
    void deleteByConversationId(String conversationId);

    /**
     * 删除过期记录
     */
    void deleteByCreatedAtBefore(LocalDateTime before);
}
