//let currentConversationId = null; // глобально
let currentSubscription = null;

let conversationsSubscription = null;

// Путь к WebSocket серверу
const socket = new SockJS('/ws'); // Путь по которому настраивается WebSocket в Spring

// Создаем STOMP клиент, который будет работать поверх SockJS
const stompClient = Stomp.over(socket);

// Функция для подключения
function connect() {
    console.log('Попытка подключения к WebSocket...');
    stompClient.connect({}, function (frame) {
        console.log('Подключено: ' + frame);

        // Подписка на обновления по списку разговоров
        stompClient.subscribe("/user/queue/conversations", function(message) {
            console.log("Получено сообщение о разговорах:", message.body);
            const conversations = JSON.parse(message.body);
            renderConversations(conversations); // твоя функция для рендера
        });

        // Подписка на обновление информации о собеседнике
        stompClient.subscribe('/user/queue/interlocutorInfo', function (message) {
            const interlocutorInfo = JSON.parse(message.body);
            console.log("Получена информация о собеседнике:", interlocutorInfo);
            if (interlocutorInfo && interlocutorInfo.interlocutorName && interlocutorInfo.interlocutorAvatar) {
                document.getElementById('interlocutorName').innerText = interlocutorInfo.interlocutorName;
                document.getElementById('interlocutorAvatar').src = interlocutorInfo.interlocutorAvatar;
            } else {
                console.error("Информация о собеседнике не получена:", interlocutorInfo);
            }
        });

        // Загружаем разговоры сразу после подключения
        loadConversations();
    }, function(error) {
        console.error('Ошибка WebSocket: ', error);
    });
}


// Функция для получения информации о собеседнике
function getInterlocutorInfo(conversationId) {
    console.log('Начинаю загрузку информации о собеседнике');
    stompClient.send("/app/chat.getInterlocutorInfo", {}, JSON.stringify({ conversationId: conversationId }));
}

// Функция для получения списка диалогов с сервера
function loadConversations() {
    console.log('Загружаем разговоры');
    if (stompClient && stompClient.connected) {
        stompClient.send("/app/getConversations", {}, JSON.stringify({}));
    } else {
        console.error("STOMP client не подключен. Переподключаюсь...");

        // Если соединения нет, переподключаемся
        connect(function () {
            stompClient.send("/app/getConversations", {}, JSON.stringify({}));
        });
    }
}

/**
 * Загружает историю сообщений и подписывается на новые сообщения для указанного чата
 * @param {number|string} conversationId - ID беседы
 * @param {boolean} isInitialLoad - Флаг первичной загрузки (true = очистить контейнер)
 */
function setupChatSubscription(conversationId) {
    console.log('Инициализация чата для conversationId:', conversationId);

    if (!stompClient || !stompClient.connected) {
        console.error("STOMP client не подключен");
        return;
    }

    // Подписываемся на обновления чата
    currentSubscription = stompClient.subscribe(`/topic/history.messages/${conversationId}`, (response) => {
        try {
            const data = JSON.parse(response.body);
            const messageContainer = document.getElementById("messages-container");

            // Обработка массива сообщений (история)
            if (Array.isArray(data)) {
                console.log('Получена история сообщений:', data.length, 'шт');
                messageContainer.innerHTML = ''; // Очищаем только при первой загрузке
                data.forEach(msg => showMessage(msg));
            }
            // Обработка одиночного сообщения (новое сообщение)
            else if (data.id) {
                console.log('Получено новое сообщение:', data);
                if (conversationId === currentConversationId) {
                    showMessage(data);
                } else {
                    console.log('Сообщение для другого чата (текущий:', currentConversationId, ')');
                }
            }
        } catch (e) {
            console.error('Ошибка обработки сообщения:', e, response.body);
        }
    });

    // Запрашиваем историю сообщений
    console.log('Запрос истории сообщений для чата', conversationId);
    stompClient.send(`/app/chat.loadMessages/${conversationId}`, {}, '');

    // Сохраняем ID текущего чата
    currentConversationId = conversationId;
}


// Пример функции для рендеринга списка разговоров:
/*function renderConversations(conversations) {
    console.log('Рендерим разговоры');
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
                    ${conversation.unreadCount > 0 ? `<span class="unread-count">${conversation.unreadCount}</span>` : ""}
                    ${conversation.listing ? `<span class="dialog-listing">Объявление: ${conversation.listing.localizedTitle}</span>` : ""}
                </div>
            </div>
        `;

        container.appendChild(dialogItem);
    });

    // Инициализация слушателей кликов по диалогам
    initializeDialogClickListeners();
}*/

function renderConversations(conversations) {
    console.log('Инициализация диалогов');
    const container = document.querySelector(".dialogs-list");
    container.innerHTML = "";

    if (conversations.length === 0) {
        container.innerHTML = `
            <div class="no-dialogs">
                <p>У вас пока нет сообщений.</p>
            </div>
        `;
        return;
    }

    // Рендерим каждый диалог по отдельности
    conversations.forEach(conversation => {
        updateSingleConversation(conversation);
    });

    // Подписываемся на обновления
    subscribeToConversationsUpdates();

    // Инициализируем обработчики кликов
    initializeDialogClickListeners();

    // Автовыбор первого диалога
    if (conversations.length > 0) {
        const firstDialog = document.querySelector('.dialog-item');
        if (firstDialog) firstDialog.click();
    }
}

function subscribeToConversationsUpdates() {
    // Отписываемся от предыдущей подписки, если есть
    if (conversationsSubscription) {
        conversationsSubscription.unsubscribe();
    }

    // Подписываемся на обновления диалогов
    conversationsSubscription = stompClient.subscribe("/user/queue/conversations.updates", function(message) {
        const update = JSON.parse(message.body);
        console.log("Получено обновление диалога:", update);

        // Обновляем только конкретный диалог
        updateSingleConversation(update);

        // Если это текущий открытый диалог - обновляем его содержимое
        if (currentConversationId === update.id) {
            setupChatSubscription(update.id);
        }
    });
}

function updateSingleConversation(conversation) {
    // Находим существующий элемент диалога
    let dialogItem = document.querySelector(`.dialog-item[data-conversation-id="${conversation.id}"]`);

    // Если диалога нет в DOM - создаем новый
    if (!dialogItem) {
        dialogItem = document.createElement("div");
        dialogItem.classList.add("dialog-item");
        dialogItem.setAttribute("data-conversation-id", conversation.id);
        document.querySelector(".dialogs-list").appendChild(dialogItem);
    }

    // Обновляем содержимое диалога
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
                ${conversation.unreadCount > 0 ? `<span class="unread-count">${conversation.unreadCount}</span>` : ""}
                ${conversation.listing ? `<span class="dialog-listing">Объявление: ${conversation.listing.localizedTitle}</span>` : ""}
            </div>
        </div>
    `;

    // Переинициализируем обработчики кликов для этого диалога
    initializeDialogClickListeners(dialogItem);
}

// Функция для отправки сообщения
function sendMessage() {
    console.log('Отправляем сообщение');
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

    document.getElementById('message-input').focus();
}


// Функция для отображения нового сообщения на странице
function showMessage(message) {
    console.log('Показываем сообщения в диалоге');
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
/*function initializeDialogClickListeners() {
    console.log('Инициализируем клики по диалогам');
    const dialogItems = document.querySelectorAll('.dialog-item');

    dialogItems.forEach(item => {
        item.addEventListener('click', function () {
            const selectedId = this.getAttribute('data-conversation-id');

            // Проверяем, что выбран новый разговор
            console.log('Проверяем, что выбран новый разговор');
            if (currentConversationId !== selectedId) {
                currentConversationId = selectedId;

                // Снимаем выделение со всех, добавляем на выбранный
                dialogItems.forEach(d => d.classList.remove('active'));
                this.classList.add('active');

                // Проверяем, существует ли контейнер для сообщений
                let messageContainer = document.getElementById('messages-container');
                if (!messageContainer) {
                    messageContainer = document.createElement('div');
                    messageContainer.id = 'messages-container';
                    document.body.appendChild(messageContainer); // Или добавь в нужный контейнер на странице
                }

                // Очищаем текущие сообщения
                messageContainer.innerHTML = '';

                // Подписываемся на новый канал для текущего разговора
                if (stompClient.connected) {
                    if (currentSubscription) {
                        currentSubscription.unsubscribe();
                    }

                    console.log('Подписываемся на новый канал для текущего разговора (' + currentConversationId + ')');
                    currentSubscription = stompClient.subscribe('/topic/messages/' + currentConversationId, function (messageOutput) {
                        showMessage(JSON.parse(messageOutput.body)); // Отображаем сообщение
                    });
                }

                // Загружаем историю сообщений для текущего разговора
                console.log('Загружаем историю сообщений для текущего разговора(' + currentConversationId + ')');
                setupChatSubscription(currentConversationId);

                // Отправляем на сервер, что мы прочитали сообщения
                stompClient.send("/app/chat.markAsRead", {}, JSON.stringify({
                    conversationId: currentConversationId
                }));

                // Получаем информацию о собеседнике
                console.log('Получаем информацию о собеседнике после клика на диалог');
                getInterlocutorInfo(currentConversationId);
            } else {
                console.log('Динахуй');
            }
        });
    });

    // 🔁 Автоматически нажать на первый элемент, если они есть
    if (dialogItems.length > 0) {
        dialogItems[0].click();  // Этот клик сработает сразу после рендера
    }
}*/
function initializeDialogClickListeners() {
    console.log('Инициализируем клики по диалогам');
    const dialogItems = document.querySelectorAll('.dialog-item');

    dialogItems.forEach(item => {
        // Удаляем предыдущие обработчики
        item.removeEventListener('click', handleDialogClick);
        item.addEventListener('click', handleDialogClick);
    });
}

function handleDialogClick() {
    const selectedId = this.getAttribute('data-conversation-id');

    // Проверяем, что выбран новый разговор
    if (currentConversationId !== selectedId) {
        currentConversationId = selectedId;

        // Снимаем выделение со всех, добавляем на выбранный
        document.querySelectorAll('.dialog-item').forEach(d => d.classList.remove('active'));
        this.classList.add('active');

        // Очищаем контейнер сообщений
        const messageContainer = document.getElementById('messages-container') ||
            createMessageContainer();
        messageContainer.innerHTML = '';

        if (stompClient.connected) {
            if (currentSubscription) {
                currentSubscription.unsubscribe();
            }

            console.log('Подписываемся на новый канал для текущего разговора (' + currentConversationId + ')');
            currentSubscription = stompClient.subscribe('/topic/messages/' + currentConversationId, function (messageOutput) {
                showMessage(JSON.parse(messageOutput.body)); // Отображаем сообщение
            });
        }

        // Подписываемся на новый канал
        setupChatSubscription(currentConversationId);

        // Отправляем на сервер, что мы прочитали сообщения
        stompClient.send("/app/chat.markAsRead", {}, JSON.stringify({
            conversationId: currentConversationId
        }));

        // Получаем информацию о собеседнике
        getInterlocutorInfo(currentConversationId);
    }
}

function createMessageContainer() {
    const container = document.createElement('div');
    container.id = 'messages-container';
    document.body.appendChild(container);
    return container;
}

function initializeEventHandlers() {
    // Обработчик кнопки отправки
    document.getElementById('send-btn').addEventListener('click', sendMessage);

    // Обработчик Enter в поле ввода
    document.getElementById('message-input').addEventListener('keydown', function(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });
}

// Вызов функции подключения
connect();

// Инициализация обработчиков событий
initializeEventHandlers();
