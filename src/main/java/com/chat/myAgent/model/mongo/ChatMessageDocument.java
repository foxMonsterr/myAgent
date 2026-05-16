package com.chat.myAgent.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
@CompoundIndex(name = "idx_session_created_at", def = "{'sessionId': 1, 'createdAt': 1}")
@CompoundIndex(name = "idx_user_created_at", def = "{'userId': 1, 'createdAt': -1}")
public class ChatMessageDocument {

    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";
    public static final String ROLE_SYSTEM = "system";
    public static final String ROLE_TOOL = "tool";

    @Id
    private String id;

    @Indexed(unique = true)
    private String messageId;

    @Indexed
    private String sessionId;

    @Indexed
    private String userId;

    private String role;

    private String content;

    private String traceId;

    private String model;

    private List<Map<String, Object>> toolCalls;

    private Long latencyMs;

    private Map<String, Object> tokenUsage;

    private LocalDateTime createdAt;
}
