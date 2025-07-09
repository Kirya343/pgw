const params = new URLSearchParams(window.location.search);

const userLocale = /*[[${locale}]]*/ 'en';

let conversationIdFromUrl = params.get('conversationId') || null;

let currentSubscription = null;

let conversationsSubscription = null;

// Путь к WebSocket серверу
const socket = new SockJS('/ws'); // Путь по которому настраивается WebSocket в Spring

// Создаем STOMP клиент, который будет работать поверх SockJS
const stompClient = Stomp.over(socket);

// Функция для подключения
function connect() {
    stompClient.connect({}, function (frame) {

        // Подписка на обновления по списку разговоров
        stompClient.subscribe("/user/queue/conversations", function(message) {
            const conversation = JSON.parse(message.body);
            addConversationToList(conversation);
        });

        subscribeToConversationsUpdates();

        // Подписка на обновление информации о собеседнике
        stompClient.subscribe('/user/queue/interlocutorInfo', function (message) {
            const interlocutorInfo = JSON.parse(message.body);
            if (interlocutorInfo && interlocutorInfo.name && interlocutorInfo.avatar) {
                document.getElementById('interlocutorName').innerText = interlocutorInfo.name;
                document.getElementById('interlocutorAvatar').src = interlocutorInfo.avatar;
            }
        });

        // Загружаем разговоры сразу после подключения
        loadConversations();
    });
}


// Функция для получения информации о собеседнике
function getInterlocutorInfo(conversationId) {
    stompClient.send("/app/chat.getInterlocutorInfo", {}, JSON.stringify({ conversationId: conversationId }));
}

// Функция для получения списка диалогов с сервера
function loadConversations() {
    if (stompClient && stompClient.connected) {
        stompClient.send("/app/getConversations", {"locale": userLocale}, JSON.stringify({}));
    } else {
        // Если соединения нет, переподключаемся
        connect(function () {
            stompClient.send("/app/getConversations", {"locale": userLocale}, JSON.stringify({}));
        });
    }
}

/**
 * Загружает историю сообщений и подписывается на новые сообщения для указанного чата
 * @param {number|string} conversationId - ID беседы
 * @param {boolean} isInitialLoad - Флаг первичной загрузки (true = очистить контейнер)
 */
function setupChatSubscription(conversationId) {
    // Подписываемся на обновления чата
    currentSubscription = stompClient.subscribe(`/topic/history.messages/${conversationId}`, (response) => {
        try {
            const data = JSON.parse(response.body);
            const messageContainer = document.getElementById("messages-container");

            // Обработка массива сообщений (история)
            if (Array.isArray(data)) {
                messageContainer.innerHTML = ''; // Очищаем только при первой загрузке
                data.forEach(msg => showMessage(msg));
            }
            // Обработка одиночного сообщения (новое сообщение)
            else if (data.id) {
                if (conversationId === currentConversationId) {
                    showMessage(data);
                }
            }
        } catch (error) {
            console.error("Ошибка при обработке сообщения:", error);
        }
    });

    // Запрашиваем историю сообщений
    stompClient.send(`/app/chat.loadMessages/${conversationId}`, {}, '');

    // Сохраняем ID текущего чата
    currentConversationId = conversationId;
}


// Пример функции для рендеринга списка разговоров:
function addConversationToList(conversation) {

    const container = document.querySelector(".dialogs-list");

    const noDialogs = document.getElementById("no-dialogs");
    if (noDialogs) {
        noDialogs.style.display = 'none';
    }

    updateSingleConversation(conversation);

    // Снова инициализируем обработчики кликов
    initializeDialogClickListeners();

    // Автовыбор первого диалога (теперь это будет самый активный)
    if (conversationIdFromUrl) {
        if (conversation.id == conversationIdFromUrl) {
            let firstDialog = container.querySelector(`[data-conversation-id="${conversation.id}"]`);
            if (firstDialog) firstDialog.click();
        }
    } else { 
        const firstDialog = document.querySelector('.dialog-item');
        firstDialog.getAttribute('data-conversation-id')
        if (firstDialog != currentConversationId) firstDialog.click();
    }
}

function subscribeToConversationsUpdates() {
    if (conversationsSubscription) {
        conversationsSubscription.unsubscribe();
    }

    conversationsSubscription = stompClient.subscribe("/user/queue/conversations.updates", function(message) {
        const update = JSON.parse(message.body);

        // Обновляем только конкретный диалог
        updateSingleConversation(update);

        // Если это текущий открытый диалог - обновляем его содержимое
        if (currentConversationId === update.id) {
            setupChatSubscription(update.id);
        }

        // Перемещаем наверх ТОЛЬКО если есть новое сообщение
        if (update.hasNewMessage) { // Добавьте это поле в ConversationDTO на сервере
            moveConversationToTop(update.id);
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

    let formattedDate = "";
    if (conversation.lastMessageTime) {
        const date = new Date(conversation.lastMessageTime);
        formattedDate = date.toLocaleDateString('ru-RU'); // формат: 20.05.2025
    }

    // Обновляем содержимое диалога
    dialogItem.innerHTML = `
        <img class="avatar p50-avatar" src="${conversation.interlocutorAvatar}" 
                onerror="this.src='/images/avatar-placeholder.png'" 
                alt="Аватар">
        <div class="dialog-content">
            <div class="dialog-header">
                <h4>${conversation.interlocutorName}</h4>
                <span class="dialog-time">${formattedDate}</span>
            </div>
            <p class="dialog-preview">${conversation.lastMessagePreview || ""}</p>
            <div class="dialog-meta">
                ${conversation.unreadCount > 0 ? `<span class="unread-count">${conversation.unreadCount}</span>` : ""}
                ${conversation.listing ? `<span class="dialog-listing">${conversation.listing.localizedTitle}</span>` : ""}
            </div>
        </div>
    `;

    // Переинициализируем обработчики кликов для этого диалога
    initializeDialogClickListeners(dialogItem);
}

// Функция для отправки сообщения
function sendMessage() {
    // Проверяем, есть ли выбранный диалог
    if (!currentConversationId) {
        alert("Пожалуйста, выберите диалог для отправки сообщения");
        return;
    }

    const messageText = document.getElementById('message-input').value.trim();
    
    // Проверяем, что сообщение не пустое
    if (!messageText) return;

    const message = {
        text: messageText,
        senderId: currentUserId,
        conversationId: currentConversationId
    };

    // Отправляем сообщение через STOMP
    stompClient.send("/app/chat.send", {"locale": userLocale}, JSON.stringify(message), userLocale);
    
    // Очищаем поле ввода
    document.getElementById('message-input').value = '';
    document.getElementById('message-input').focus();
}

function updateMessageInputState() {
    const messageInput = document.getElementById('message-input');
    const sendBtn = document.getElementById('send-btn');
    
    if (!currentConversationId) {
        // Блокируем ввод и кнопку, если диалог не выбран
        messageInput.disabled = true;
        messageInput.placeholder = "Выберите диалог для отправки сообщения";
        sendBtn.disabled = true;
    } else {
        // Разблокируем, если диалог выбран
        messageInput.disabled = false;
        messageInput.placeholder = "Напишите сообщение...";
        sendBtn.disabled = false;
    }
}

// Функция для отображения нового сообщения на странице
function showMessage(message) {

    // Создаем новый элемент сообщения
    const messageContainer = document.getElementById("messages-container");
    const messageElement = document.createElement("div");
    const date = new Date(message.sentAt);
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const formattedTime = `${hours}:${minutes}`;
    
    messageElement.className = message.senderId === currentUserId ? 'message-out' : 'message-in'; // Определяем, собственное ли сообщение
    messageElement.innerHTML = `
        <div class="message-content">
            <p>${message.text}</p>
            <span class="message-time">${formattedTime}</span>
        </div>
    `;
    messageContainer.appendChild(messageElement);

    // Прокручиваем контейнер сообщений вниз
    messageContainer.scrollTop = messageContainer.scrollHeight;
}


function initializeDialogClickListeners() {
    const dialogItems = document.querySelectorAll('.dialog-item');
    const dialogsList = document.querySelector('.dialogs-list');

    dialogItems.forEach(item => {
        // Удаляем предыдущие обработчики
        item.removeEventListener('click', handleDialogClick);
        item.addEventListener('click', handleDialogClick);
        dialogsList.classList.remove('show');
    });
}

function handleDialogClick() {
    const selectedId = this.getAttribute('data-conversation-id');

    // Проверяем, что выбран новый разговор
    if (currentConversationId !== selectedId) {
        currentConversationId = selectedId;

        // Обновляем состояние поля ввода
        updateMessageInputState();

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
            currentSubscription = stompClient.subscribe('/topic/messages/' + currentConversationId, function (messageOutput) {
                showMessage(JSON.parse(messageOutput.body)); // Отображаем сообщение
            });
        }

        // Подписываемся на новый канал
        setupChatSubscription(currentConversationId);

        // Отправляем на сервер, что мы прочитали сообщения
        stompClient.send("/app/chat.markAsRead", {"locale": userLocale}, JSON.stringify({
            conversationId: currentConversationId
        }), userLocale);

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

function moveConversationToTop(conversationId) {
    const dialogItem = document.querySelector(`.dialog-item[data-conversation-id="${conversationId}"]`);
    if (!dialogItem) return;

    const dialogsList = document.querySelector(".dialogs-list");
    if (dialogsList.firstChild !== dialogItem) {
        dialogItem.classList.add('highlight');
        setTimeout(() => dialogItem.classList.remove('highlight'), 300);
        dialogsList.prepend(dialogItem);
    }
}

function initializeEventHandlers() {
    // Обновляем состояние поля ввода
    updateMessageInputState();

    // Обработчик кнопки отправки
    document.getElementById('send-btn').addEventListener('click', sendMessage);

    // Обработчик Enter в поле ввода
    document.getElementById('message-input').addEventListener('keydown', function(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            if (!currentConversationId) {
                alert("Пожалуйста, выберите диалог для отправки сообщения");
                return;
            }
            sendMessage();
        }
    });
}

// Вызов функции подключения
connect();

// Инициализация обработчиков событий
initializeEventHandlers();
