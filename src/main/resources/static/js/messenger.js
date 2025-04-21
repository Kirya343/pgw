//ПЕРЕКЛЮЧЕНИЕ ДИАЛОГОВ РАБОТАЕТ, НЕ ТРОГАТЬ
document.addEventListener('DOMContentLoaded', function() {
    // Инициализация переменных
    const urlParams = new URLSearchParams(window.location.search);
    let currentConversationId = urlParams.get('conversationId') ||
        document.currentScript?.getAttribute('data-conversation-id') ||
        null;
    const currentUserId = document.currentScript?.getAttribute('data-user-id') || null;

    console.log('Initial conversationId:', currentConversationId);

    // Инициализация при загрузке
    if (currentConversationId) {
        loadMessages(currentConversationId);
        updateChatInfo(currentConversationId);
    }

    // Делегирование событий для кликов по диалогам
    document.body.addEventListener('click', function(e) {
        const dialogItem = e.target.closest('.dialog-item');
        if (!dialogItem) return;

        const newConvId = dialogItem.getAttribute('data-conversation-id');
        console.log('Clicked dialog with ID:', newConvId);

        if (!newConvId || newConvId === currentConversationId) return;

        currentConversationId = newConvId;
        console.log('Switching to conversation:', currentConversationId);

        window.history.pushState(
            { conversationId: currentConversationId },
            '',
            `/secure/messenger?conversationId=${currentConversationId}`
        );

        // Обновляем информацию о собеседнике перед загрузкой сообщений
        updateChatInfo(currentConversationId);
        loadMessages(currentConversationId);
        markMessagesAsRead(currentConversationId);
    });

    // Функция обновления информации о собеседнике
    function updateChatInfo(conversationId) {
        if (!conversationId) return;

        // Берем данные из выбранного диалога в списке
        const dialogItem = document.querySelector(`.dialog-item[data-conversation-id="${conversationId}"]`);
        if (!dialogItem) return;

        const avatar = dialogItem.querySelector('.dialog-avatar img').src;
        const name = dialogItem.querySelector('h4').textContent;

        // Обновляем информацию в правой части чата
        document.querySelector('.chat-avatar').src = avatar;
        document.querySelector('.chat-user h4').textContent = name;

        console.log('Chat info updated for conversation:', conversationId);
    }

    // Функция загрузки сообщений
    async function loadMessages(convId) {
        console.log('Fetching messages for conversation:', convId);
        try {
            const response = await fetch(`/secure/messenger/getMessages?conversationId=${convId}`);
            if (!response.ok) throw new Error('Failed to load messages');

            const html = await response.text();
            const container = document.querySelector('.messages-container');
            if (container) {
                container.innerHTML = html;
                console.log('Messages loaded successfully');
            }
        } catch (error) {
            console.error('Error loading messages:', error);
        }
    }

    // ========== ДОБАВЛЯЕМ ОТПРАВКУ СООБЩЕНИЙ ==========

    // Обработчики для кнопки отправки и клавиши Enter
    document.querySelector('.send-btn')?.addEventListener('click', sendMessage);
    document.querySelector('.message-input')?.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') sendMessage();
    });

    // Функция отправки сообщения
    function sendMessage() {
        const input = document.querySelector('.message-input');
        const text = input?.value.trim();

        if (!currentConversationId || !text) return;

        fetch('/secure/messenger/send', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            },
            body: `conversationId=${currentConversationId}&text=${encodeURIComponent(text)}`
        })
            .then(response => {
                if (response.ok) {
                    input.value = ''; // Очищаем поле ввода
                    loadMessages(currentConversationId); // Перезагружаем сообщения
                }
            })
            .catch(error => console.error('Send error:', error));
    }
    // ========== ДОБАВЛЯЕМ ОТМЕТКУ ПРОЧИТАННЫХ СООБЩЕНИЙ ==========

    // Функция отметки сообщений как прочитанных
    function markMessagesAsRead(conversationId) {
        if (!conversationId) return;

        fetch('/secure/messenger/markAsRead', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            },
            body: `conversationId=${conversationId}`
        })
            .then(response => {
                if (response.ok) {
                    console.log('Messages marked as read');
                    // Обновляем счетчик непрочитанных в UI
                    const unreadBadge = document.querySelector(`.dialog-item[data-conversation-id="${conversationId}"] .unread-count`);
                    if (unreadBadge) {
                        unreadBadge.remove();
                    }
                }
            })
            .catch(error => console.error('Error marking messages as read:', error));
    }
});