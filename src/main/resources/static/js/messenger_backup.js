// В самом начале файла добавьте
let currentConversationId = null;
let currentUserId = null;

document.addEventListener('DOMContentLoaded', function() {
    // Инициализация переменных из Thymeleaf
    currentConversationId = document.currentScript?.getAttribute('data-conversation-id') || null;
    currentUserId = document.currentScript?.getAttribute('data-user-id') || null;
    // WebSocket подключение
    const socket = new SockJS('/ws-chat');
    const stompClient = Stomp.over(socket);

    // Подключение WebSocket
    stompClient.connect({}, function(frame) {
        console.log('WebSocket connected');

        stompClient.subscribe('/topic/messages', function(message) {
            const msg = JSON.parse(message.body);
            console.log('Received WebSocket message:', msg);

            if (msg.conversationId == currentConversationId) {
                addMessageToChat({
                    text: msg.text,
                    sentAt: msg.sentAt,
                    sender: { id: msg.senderId },
                    isOwn: msg.senderId == currentUserId
                });
            }
        });
    }, function(error) {
        console.error('WebSocket error:', error);
    });

    stompClient.onDisconnect = function() {
        console.log('WebSocket disconnected, trying to reconnect...');
        setTimeout(connectWebSocket, 5000);
    };

    // Обработчик клика по диалогу
    function handleDialogClick(e) {
        const dialogItem = e.target.closest('.dialog-item');
        if (!dialogItem) {
            console.log('Clicked element is not a dialog item');
            return;
        }

        const newConvId = dialogItem.getAttribute('data-conversation-id');
        console.log('Dialog clicked, conversationId:', newConvId);

        if (!newConvId) {
            console.error('Dialog item has no data-conversation-id attribute');
            return;
        }

        if (newConvId === currentConversationId) {
            console.log('Same conversation selected, ignoring');
            return;
        }

        console.log(`Switching conversation from ${currentConversationId} to ${newConvId}`);
        currentConversationId = newConvId;

        // Обновляем URL
        history.pushState({convId: currentConversationId}, '',
            `/secure/messenger?conversationId=${currentConversationId}`);

        // Загружаем сообщения
        loadMessages(currentConversationId)
            .then(() => {
                updateActiveDialog(currentConversationId);
                updateChatInfo(currentConversationId);
                console.log('Conversation switched successfully');
            })
            .catch(error => {
                console.error('Error switching conversation:', error);
            });
    }

// Вешаем обработчик после полной загрузки DOM
    document.addEventListener('DOMContentLoaded', function() {
        const dialogsList = document.querySelector('.dialogs-list');
        if (dialogsList) {
            dialogsList.addEventListener('click', handleDialogClick);
        } else {
            console.error('Dialogs list element not found');
        }
    });

    // Обработчик навигации (назад/вперед)
    window.addEventListener('popstate', function(event) {
        const urlParams = new URLSearchParams(window.location.search);
        const convId = urlParams.get('conversationId');

        if (convId && convId !== currentConversationId) {
            currentConversationId = convId;
            loadMessages(currentConversationId);
            updateActiveDialog(currentConversationId);
        }
    });

    // Функция обновления активного диалога
    function updateActiveDialog(convId) {
        document.querySelectorAll('.dialog-item').forEach(item => {
            if (item.getAttribute('data-conversation-id') == convId) {
                item.classList.add('active');
            } else {
                item.classList.remove('active');
            }
        });
    }

    // Функция добавления сообщения в чат
    function addMessageToChat(message) {
        const container = document.querySelector('.messages-container');
        if (!container) return;

        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${message.isOwn ? 'message-out' : 'message-in'}`;

        messageDiv.innerHTML = `
            <div class="message-content">
                <p>${message.text}</p>
                <span class="message-time">
                    ${formatTime(message.sentAt)}
                </span>
            </div>
        `;

        container.appendChild(messageDiv);
        scrollToBottom();
    }

    // Форматирование времени
    function formatTime(dateString) {
        const date = new Date(dateString);
        return date.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
    }

    // Загрузка сообщений
    function loadMessages(convId) {
        fetch(`/secure/messenger/getMessages?conversationId=${convId}`)
            .then(response => {
                if (!response.ok) throw new Error('Network error');
                return response.text();
            })
            .then(html => {
                const container = document.querySelector('.messages-container');
                if (container) {
                    container.innerHTML = html;
                    scrollToBottom();
                }
            })
            .catch(error => console.error('Error loading messages:', error));
    }

    // Отправка сообщения
    document.querySelector('.send-btn')?.addEventListener('click', sendMessage);
    document.querySelector('.message-input')?.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') sendMessage();
    });

    function sendMessage() {
        const input = document.querySelector('.message-input');
        const text = input?.value.trim();

        if (!currentConversationId || !text) return;

        try {
            stompClient.send("/app/chat.send", {},
                JSON.stringify({
                    text: text,
                    conversationId: currentConversationId
                }));

            input.value = '';
            input.focus();
        } catch (error) {
            console.error('Send error:', error);
            alert('Не удалось отправить сообщение');
        }
    }

    function updateChatInfo(convId) {
        const dialogItem = document.querySelector(`.dialog-item[data-conversation-id="${convId}"]`);
        if (dialogItem) {
            const avatar = dialogItem.querySelector('.dialog-avatar img').src;
            const name = dialogItem.querySelector('h4').textContent;

            document.querySelector('.chat-avatar').src = avatar;
            document.querySelector('.chat-user h4').textContent = name;

            // Сбрасываем счетчик непрочитанных
            const unreadBadge = dialogItem.querySelector('.unread-count');
            if (unreadBadge) {
                unreadBadge.remove();
            }
        }
    }

    // Прокрутка вниз
    function scrollToBottom() {
        const container = document.querySelector('.messages-container');
        if (container) container.scrollTop = container.scrollHeight;
    }

    // Инициализация при загрузке
    if (currentConversationId) {
        loadMessages(currentConversationId);
        updateActiveDialog(currentConversationId);
    }
});