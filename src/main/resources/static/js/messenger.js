//ПЕРЕКЛЮЧЕНИЕ ДИАЛОГОВ РАБОТАЕТ, НЕ ТРОГАТЬ
document.addEventListener('DOMContentLoaded', function() {
    // Инициализация переменных
    const urlParams = new URLSearchParams(window.location.search);
    let currentConversationId = urlParams.get('conversationId') ||
        document.currentScript?.getAttribute('data-conversation-id') ||
        null;
    const currentUserId = document.currentScript?.getAttribute('data-user-id') || null;

    console.log('Initial conversationId:', currentConversationId);

    // Делегирование событий для кликов по диалогам
    document.body.addEventListener('click', function(e) {
        const dialogItem = e.target.closest('.dialog-item');
        if (!dialogItem) return;

        const newConvId = dialogItem.getAttribute('data-conversation-id');
        console.log('Clicked dialog with ID:', newConvId);

        if (!newConvId || newConvId === currentConversationId) return;

        currentConversationId = newConvId;
        console.log('Switching to conversation:', currentConversationId);

        // Обновляем URL без перезагрузки страницы
        window.history.pushState(
            { conversationId: currentConversationId },
            '',
            `/secure/messenger?conversationId=${currentConversationId}`
        );

        // Загружаем сообщения
        loadMessages(currentConversationId);
    });

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

    // Инициализация при загрузке
    if (currentConversationId) {
        loadMessages(currentConversationId);
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
});