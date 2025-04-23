//let currentConversationId = null; // глобально
let currentSubscription = null;

// Путь к WebSocket серверу
const socket = new SockJS('/ws'); // Путь по которому настраивается WebSocket в Spring

// Создаем STOMP клиент, который будет работать поверх SockJS
const stompClient = Stomp.over(socket);

// Функция для подключения
function connect() {
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);

        initializeDialogClickListeners();

    });
}

function loadMessagesForConversation(conversationId) {
    fetch('/secure/messenger/' + conversationId)
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка при загрузке сообщений: ' + response.statusText);
            }
            return response.json();
        })
        .then(messages => {
            if (Array.isArray(messages)) {
                if (messages.length === 0) {
                    console.log('Сообщений в чате нет');
                }
                messages.forEach(message => {
                    showMessage(message);
                    // Ваш код для обработки сообщений
                });
            } else {
                console.error('Ожидался массив сообщений, но получены данные:', messages);
            }
        })
        .catch(error => {
            console.error('Ошибка при загрузке сообщений:', error);
        });
}

document.querySelectorAll('.chat-item').forEach(item => {
    item.addEventListener('click', function () {
        const newConversationId = this.dataset.conversationId;

        if (newConversationId !== currentConversationId) {
            currentConversationId = newConversationId;

            // Отписываемся от старого и подписываемся на новый канал
            if (currentSubscription) {
                currentSubscription.unsubscribe();
            }

            currentSubscription = stompClient.subscribe('/topic/messages.' + currentConversationId, function (messageOutput) {
                showMessage(JSON.parse(messageOutput.body));
            });

            // Тут можно также загрузить историю сообщений, если нужно
            loadMessagesForConversation(currentConversationId);
        }
    });
});

// Функция для отправки сообщения
function sendMessage() {
    const messageText = document.getElementById('message-input').value;
    if (!messageText.trim()) {
        console.error("Message text cannot be empty.");
        return;
    }

    // Проверка существования ID пользователя и ID чата
    if (!currentUserId || !currentConversationId) {
        console.error("User or conversation ID is missing.");
        return;
    }

    const message = {
        text: messageText,
        senderId: currentUserId,
        conversationId: currentConversationId
    };

    // Отправляем сообщение через STOMP
    stompClient.send("/app/chat.send", {}, JSON.stringify(message));

    // Очищаем поле ввода
    document.getElementById('message-input').value = '';
}


// Функция для отображения нового сообщения на странице
function showMessage(message) {
    console.log('Received message: ', message);

    // Создаем новый элемент сообщения
    const messageContainer = document.getElementById("messages-container");
    const messageElement = document.createElement("div");
    messageElement.className = message.senderId === currentUserId ? 'message-out' : 'message-in'; // Определяем, собственное ли сообщение
    messageElement.innerHTML = `
        <div class="message-content">
            <p>${message.text}</p>
            <span class="message-time">${new Date(message.sentAt).toLocaleTimeString()}</span>
        </div>
    `;
    messageContainer.appendChild(messageElement);

    // Прокручиваем контейнер сообщений вниз
    messageContainer.scrollTop = messageContainer.scrollHeight;
}

// При подключении STOMP (после stompClient.connect)
function initializeDialogClickListeners() {
    const dialogItems = document.querySelectorAll('.dialog-item');

    dialogItems.forEach(item => {
        item.addEventListener('click', function () {
            const selectedId = this.getAttribute('data-conversation-id');

            // Проверяем, что выбран новый разговор
            if (currentConversationId !== selectedId) {
                currentConversationId = selectedId;

                // Снимаем выделение со всех, добавляем на выбранный
                dialogItems.forEach(d => d.classList.remove('active'));
                this.classList.add('active');

                // Очищаем текущие сообщения
                document.getElementById('messages-container').innerHTML = '';

                // Проверка на подключение STOMP
                if (stompClient.connected) {
                    // Отписываемся от предыдущей подписки, если она есть
                    if (currentSubscription) {
                        currentSubscription.unsubscribe();
                    }

                    // Подписываемся на новый канал для текущего разговора
                    currentSubscription = stompClient.subscribe('/topic/messages.' + currentConversationId, function (messageOutput) {
                        showMessage(JSON.parse(messageOutput.body)); // Отображаем сообщение
                    });
                }

                // Загружаем историю сообщений для текущего разговора
                loadMessagesForConversation(currentConversationId);

                // Отправляем на сервер, что мы прочитали сообщения
                stompClient.send("/app/chat.markAsRead", {}, JSON.stringify({
                    conversationId: currentConversationId
                }));
            }
        });
    });

    // 🔁 Автоматически нажать на первый элемент, если они есть
    if (dialogItems.length > 0) {
        dialogItems[0].click();
    }
}


// Вызов функции подключения
connect();

// Привязка кнопки отправки
document.getElementById('send-btn').addEventListener('click', sendMessage);
