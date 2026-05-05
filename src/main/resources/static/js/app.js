// ==================== 辅助函数（必须放在最前面） ====================

/**
 * 生成唯一ID
 */
function generateId() {
    return 'conv_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
}

/**
 * 添加消息到聊天区域
 */
function appendMessage(role, content, meta = '') {
    const chatArea = document.getElementById('chatArea');
    const msgDiv = document.createElement('div');
    msgDiv.className = `message ${role}`;

    const metaHtml = meta ? `<div class="meta">${meta}</div>` : '';

    msgDiv.innerHTML = `
        <div class="avatar">${role === 'user' ? '👤' : role === 'assistant' ? '🤖' : '⚠️'}</div>
        <div class="message-content">
            ${metaHtml}
            <div class="content">${content}</div>
        </div>
    `;

    chatArea.appendChild(msgDiv);
    scrollToBottom();
    return msgDiv;
}

/**
 * 设置状态文本
 */
function setStatus(text) {
    document.getElementById('statusText').textContent = text;
}

/**
 * 禁用/启用发送按钮
 */
function disableSend(disabled) {
    document.getElementById('sendBtn').disabled = disabled;
    document.getElementById('messageInput').disabled = disabled;
}

/**
 * 滚动到底部
 */
function scrollToBottom() {
    const chatArea = document.getElementById('chatArea');
    chatArea.scrollTop = chatArea.scrollHeight;
}

/**
 * 新会话
 */
function newSession() {
    currentConversationId = generateId();
    document.getElementById('conversationId').value = currentConversationId;
    document.getElementById('chatArea').innerHTML = `
        <div class="welcome-message">
            <p> 你好！我是 SmartAgent，一个全能型智能助手。</p>
            <p>我可以：💬 多轮对话 | 🔧 调用工具 | 📚 知识库问答 | 📋 任务规划</p>
            <p>选择上方的模式开始对话吧！</p>
        </div>
    `;
    setStatus('就绪');
}

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
        appendMessage('system', ' 请求失败: ' + error.message);
        console.error('Error:', error);
    } finally {
        setStatus('就绪');
        disableSend(false);
    }
}

// ==================== 普通模式 ====================
async function handleNormalMode(mode, message, convId) {
    const config = getModeConfig(mode, message, convId);

    if (!config) {
        appendMessage('system', '️ 未配置该模式');
        return;
    }

    const response = await fetch(config.url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(config.body)
    });

    // 检查 HTTP 状态
    if (!response.ok) {
        if (response.status === 403) {
            appendMessage('system', ' 403 禁止访问：接口需要认证，请联系管理员或检查 Security 配置');
            console.error('403 Forbidden - 需要 JWT Token');
            return;
        }
        appendMessage('system', ` HTTP ${response.status} ${response.statusText}`);
        return;
    }

    // 检查响应是否为空
    const text = await response.text();
    if (!text) {
        appendMessage('system', ' 服务器返回空响应');
        return;
    }

    let result;
    try {
        result = JSON.parse(text);
    } catch (e) {
        appendMessage('system', ' 响应解析失败: ' + e.message);
        console.error('JSON Parse Error:', text);
        return;
    }

    if (result.code === 200 && result.data) {
        const data = result.data;
        let reply = '';

        // 根据不同模式提取回复
        if (data.reply) {
            reply = data.reply;
        } else if (data.answer) {
            reply = data.answer;
            if (data.sources && data.sources.length > 0) {
                reply += '\n\n 来源: ' + data.sources.join(', ');
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
// ... existing code ...


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
                body: { task: message, conversationId: convId }
            };
        case 'full':
            return {
                url: '/api/v1/agent/chat',
                body: { message, conversationId: convId }
            };
        default:
            return null;
    }
}
