package com.chat.myAgent.repository;

import com.chat.myAgent.model.entity.AgentInvocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentInvocationRepository extends JpaRepository<AgentInvocationEntity, Long> {

    List<AgentInvocationEntity> findByConversationIdOrderByCreatedAtDesc(String conversationId);

    long countByAgentType(String agentType);
}
