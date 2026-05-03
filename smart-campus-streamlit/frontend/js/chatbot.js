// chatbot.js
let chatOpen = false;

function toggleChatbot() {
    chatOpen = !chatOpen;
    const container = document.getElementById('chatbotContainer');
    const badge = document.querySelector('.chat-badge');
    if (container) container.classList.toggle('open', chatOpen);
    if (badge) badge.style.display = chatOpen ? 'none' : 'flex';
}

async function sendChatMessage() {
    const input = document.getElementById('chatInput');
    const msg = input.value.trim();
    if (!msg) return;
    appendMessage(msg, 'user');
    input.value = '';
    appendTypingIndicator();
    try {
        const sessionId = localStorage.getItem('chat_session') || 'sess_' + Date.now();
        localStorage.setItem('chat_session', sessionId);
        const res = await API.sendChatMessage({ message: msg, sessionId });
        removeTypingIndicator();
        appendMessage(res.response || "Sorry, I couldn't process that.", 'bot');
    } catch(e) {
        removeTypingIndicator();
        appendMessage("I'm having trouble connecting right now. Please try again!", 'bot');
    }
}

function appendMessage(text, sender) {
    const msgs = document.getElementById('chatbotMessages');
    if (!msgs) return;
    const div = document.createElement('div');
    div.className = sender === 'bot' ? 'bot-message' : 'user-message';
    div.innerHTML = `<div class="${sender}-bubble">${text.replace(/\n/g, '<br>')}</div>`;
    msgs.appendChild(div);
    msgs.scrollTop = msgs.scrollHeight;
}

function appendTypingIndicator() {
    const msgs = document.getElementById('chatbotMessages');
    if (!msgs) return;
    const div = document.createElement('div');
    div.className = 'bot-message';
    div.id = 'typingIndicator';
    div.innerHTML = '<div class="bot-bubble"><span style="display:flex;gap:4px"><span style="animation:bounce 1s infinite">.</span><span style="animation:bounce 1s infinite 0.2s">.</span><span style="animation:bounce 1s infinite 0.4s">.</span></span></div>';
    msgs.appendChild(div);
    msgs.scrollTop = msgs.scrollHeight;
}

function removeTypingIndicator() {
    const el = document.getElementById('typingIndicator');
    if (el) el.remove();
}

function handleChatKey(e) {
    if (e.key === 'Enter') sendChatMessage();
}
