// ==================== 全局状态 ====================
let currentConversationId = generateId();

// ==================== 初始化 ====================
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('conversationId').value = currentConversationId;

    // Enter 发送，Shift+Enter 换行
    document.getElementById('messageInput').addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });
});

// ==================== 发送消息 ====================
async function sendMessage() {
    const input = document.getElementById('messageInput');
    const message = input.value.trim();
    if (!message) return;

    const mode = document.getElementById('chatMode').value;
    const convId = document.getElementById('conversationId').value || currentConversationId;

    // 显示用户消息
    appendMessage('user', message);
    input.value = '';
    setStatus('思考中...');
    disableSend(true);

    try {
        if (mode === 'stream') {
            await handleStreamMode(message, convId);
        } else {
            await handleNormalMode(mode, message, convId);
        }
    } catch (error) {
        appendMessage('system', '❌ 请求失败: ' + error.message);
    } finally {
        setStatus('就绪');
        disableSend(false);
    }
}

// ==================== 普通模式 ====================
async function handleNormalMode(mode, message, convId) {
    const config = getModeConfig(mode, message, convId);

    const response = await fetch(config.url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(config.body)
    });

    const result = await response.json();

    if (result.code === 200 && result.data) {
        const data = result.data;
        let reply = '';

        // 根据不同模式提取回复
        if (data.reply) {
            reply = data.reply;
        } else if (data.answer) {
            reply = data.answer;
            if (data.sources && data.sources.length > 0) {
                reply += '\n\n📎 来源: ' + data.sources.join(', ');
            }
        } else if (data.finalAnswer) {
            reply = data.finalAnswer;
            if (data.steps && data.steps.length > 0) {
                reply = '📋 执行了 ' + data.steps.length + ' 个步骤\n\n' + reply;
            }
        } else if (data.directAnswer) {
            reply = data.directAnswer;
        }

        let meta = '';
        if (data.agentType) meta += data.agentType;
        if (data.historySize) meta += ' | 历史:' + data.historySize + '条';
        if (data.totalTimeMs) meta += ' | 耗时:' + data.totalTimeMs + 'ms';

        appendMessage('assistant', reply, meta);
    } else {
        appendMessage('system', '⚠️ ' + (result.message || '未知错误'));
    }
}

// ==================== 流式模式 ====================
async function handleStreamMode(message, convId) {
    const url = `/api/v1/stream/chat?message=${encodeURIComponent(message)}&conversationId=${encodeURIComponent(convId)}`;

    const msgDiv = appendMessage('assistant', '', '流式输出中...');
    const contentSpan = msgDiv.querySelector('.content');

    const response = await fetch(url);
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let fullText = '';

    while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        const chunk = decoder.decode(value, { stream: true });
        // SSE 格式解析
        const lines = chunk.split('\n');
        for (const line of lines) {
            if (line.startsWith('data:')) {
                const data = line.substring(5).trim();
                if (data && data !== '[DONE]') {
                    fullText += data;
                    contentSpan.textContent = fullText;
                }
            } else if (line.trim() && !line.startsWith(':')) {
                // 非标准SSE格式，直接追加
                fullText += line;
                contentSpan.textContent = fullText;
            }
        }

        scrollToBottom();
    }

    // 流式结束后更新 meta
    const metaSpan = msgDiv.querySelector('.meta');
    if (metaSpan) metaSpan.textContent = 'stream | 完成';
}

// ==================== 模式配置映射 ====================
function getModeConfig(mode, message, convId) {
    switch (mode) {
        case 'memory':
            return {
                url: '/api/v1/chat/memory',
                body: { message, conversationId: convId }
            };
        case 'tool':
            return {
                url: '/api/v1/agent/chat',
                body: { message, conversationId: convId }
            };
        case 'rag':
            return {
                url: '/api/v1/knowledge/ask',
                body: { question: message, conversationId: convId }
            };
        case 'planning':
            return {
                url: '/api/v1/planning/execute',

            }
    }
}