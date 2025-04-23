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

        // Подписка на обновления по списку разговоров
        stompClient.subscribe("/user/queue/conversations", function(message) {
            const conversations = JSON.parse(message.body);
            renderConversations(conversations); // твоя функция для рендера
        });

        // Подписка на обновление информации о собеседнике
        stompClient.subscribe('/user/queue/interlocutorInfo', function (message) {
            const interlocutorInfo = JSON.parse(message.body);

            // Обновление UI с данными о собеседнике
            if (interlocutorInfo && interlocutorInfo.interlocutorName && interlocutorInfo.interlocutorAvatar) {
                document.getElementById('interlocutorName').innerText = interlocutorInfo.interlocutorName;
                document.getElementById('interlocutorAvatar').src = interlocutorInfo.interlocutorAvatar;
            } else {
                console.error("Информация о собеседнике не получена:", interlocutorInfo);
            }
        });

        // Инициализация слушателей кликов по диалогам
        initializeDialogClickListeners();

        // Загружаем разговоры сразу после подключения
        loadConversations();
    });
}


// Функция для получения информации о собеседнике
function getInterlocutorInfo(conversationId) {
    stompClient.send("/app/chat.getInterlocutorInfo", {}, JSON.stringify({ conversationId: conversationId }));
}

/*function loadConversations() {
    stompClient.send("/app/conversations", {}, {});
}*/

function loadMessagesForConversation(conversationId) {
    const requestedConversationId = conversationId;

    fetch('/secure/messenger/' + requestedConversationId)
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка при загрузке сообщений: ' + response.statusText);
            }
            return response.json();
        })
        .then(messages => {
            // Если пользователь уже переключился на другой чат — не отображаем
            if (requestedConversationId !== currentConversationId) {
                console.log('Пропущен ответ: пользователь уже переключился на другой чат');
                return;
            }

            const messageContainer = document.getElementById("messages-container");
            messageContainer.innerHTML = ''; // очищаем перед выводом новых

            if (Array.isArray(messages)) {
                if (messages.length === 0) {
                    console.log('Сообщений в чате нет');
                }
                messages.forEach(message => {
                    showMessage(message);
                });
            } else {
                console.error('Ожидался массив сообщений, но получены данные:', messages);
            }
        })
        .catch(error => {
            console.error('Ошибка при загрузке сообщений:', error);
        });
}

function renderConversations(conversations) {
    const container = document.querySelector(".dialogs-list");

    // Очистить текущий список
    container.innerHTML = "";

    if (conversations.length === 0) {
        container.innerHTML = `
            <div class="no-dialogs">
                <p>У вас пока нет сообщений.</p>
                <p>Начните общение, ответив на объявление или отправив сообщение пользователю.</p>
            </div>
        `;
        return;
    }

    conversations.forEach(conversation => {
        const dialogItem = document.createElement("div");
        dialogItem.classList.add("dialog-item");
        dialogItem.setAttribute("data-conversation-id", conversation.id);

        dialogItem.innerHTML = `
            <div class="dialog-avatar">
                <img src="${conversation.interlocutorAvatar}" 
                     onerror="this.src='/images/avatar-placeholder.png'" 
                     alt="Аватар">
            </div>

            <div class="dialog-content">
                <div class="dialog-header">
                    <h4>${conversation.interlocutorName}</h4>
                    <span class="dialog-time">${conversation.lastMessageTime || ""}</span>
                </div>

                <p class="dialog-preview">${conversation.lastMessagePreview || ""}</p>

                <div class="dialog-meta">
                    ${conversation.unreadCount > 0
            ? `<span class="unread-count">${conversation.unreadCount}</span>`
            : ""}
                    ${conversation.listing
            ? `<span class="dialog-listing">Объявление: ${conversation.listing.title}</span>`
            : ""}
                </div>
            </div>
        `;

        container.appendChild(dialogItem);
    });
}

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

                // Получаем информацию о собеседнике
                getInterlocutorInfo(currentConversationId);
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
