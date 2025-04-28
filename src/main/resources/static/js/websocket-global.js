// Глобальные переменные WebSocket
let isOnline = false;
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 5;

let globalStompClient = null;
let notificationSound = new Audio('/sounds/notification.mp3');
let notificationContainer = null;
let activeNotifications = [];

// Создаем контейнер для уведомлений
function createNotificationContainer() {
    notificationContainer = document.createElement('div');
    notificationContainer.id = 'notification-container';
    notificationContainer.style.cssText = `
        position: fixed;
        bottom: 20px;
        right: 20px;
        display: flex;
        flex-direction: column-reverse;
        gap: 10px;
        z-index: 1000;
    `;
    document.body.appendChild(notificationContainer);
}

// Подключение к WebSocket
function connectWebSocket() {
    try {

        const socket = new SockJS('/ws');
        globalStompClient = Stomp.over(socket);

        globalStompClient.connect({}, function (frame) {
            console.log('WebSocket подключен (глобально)');

            // Подписка на уведомления
            globalStompClient.subscribe("/user/queue/notifications", function (message) {
                const data = JSON.parse(message.body);
                if (Array.isArray(data)) {
                    // Обработка массива уведомлений
                    data.forEach(notification => {
                        showInAppNotification(notification);
                        if (!isUserInChat(notification.conversationId)) {
                            playNotificationSound();
                        }
                    });
                } else {
                    // Одиночное уведомление
                    showInAppNotification(data);
                    if (!isUserInChat(data.conversationId)) {
                        playNotificationSound();
                    }
                }
            });

        }, function (error) {
            console.error('Ошибка WebSocket:', error);
            isOnline = false;

            if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                const delay = Math.min(5000, 1000 * Math.pow(2, reconnectAttempts));
                setTimeout(connectWebSocket, delay);
                reconnectAttempts++;
            }
        });
    } catch (e) {
        console.error('Ошибка создания WebSocket соединения:', e);
        setTimeout(connectWebSocket, 5000);
    }
}

// Показ in-app уведомления с анимацией
function showInAppNotification(notification) {
    if (isUserInChat(notification.conversationId)) return;

    // Создаем контейнер при первом уведомлении
    if (!notificationContainer) {
        createNotificationContainer();
    }

    const notificationElement = document.createElement('div');
    notificationElement.className = 'in-app-notification';
    notificationElement.style.cssText = `
        position: relative;
        width: 300px;
        padding: 15px;
        background: #ffffff;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        transform: translateX(100%);
        opacity: 0;
        transition: transform 0.3s ease-out, opacity 0.3s ease-out;
        cursor: pointer;
    `;

    notificationElement.innerHTML = `
        <div class="notification-header">
            <strong>${notification.title}</strong>
            <button class="close-notification" style="
                background: none;
                border: none;
                font-size: 18px;
                cursor: pointer;
                color: #888;
                position: absolute;
                top: 5px;
                right: 5px;
            ">&times;</button>
        </div>
        <div class="notification-body">${notification.message}</div>
    `;

    // Добавляем в начало контейнера (чтобы новые были сверху)
    notificationContainer.insertBefore(notificationElement, notificationContainer.firstChild);
    activeNotifications.unshift(notificationElement);

    // Анимация появления
    setTimeout(() => {
        notificationElement.style.transform = 'translateX(0)';
        notificationElement.style.opacity = '1';
    }, 10);

    // Автозакрытие через 15 сек
    const autoCloseTimer = setTimeout(() => {
        removeNotification(notificationElement);
    }, 15000);

    // Клик для перехода в чат
    notificationElement.addEventListener('click', () => {
        clearTimeout(autoCloseTimer);
        removeNotification(notificationElement);
        window.location.href = `/secure/messenger`;
    });

    // Кнопка закрытия
    notificationElement.querySelector('.close-notification').addEventListener('click', (e) => {
        e.stopPropagation();
        clearTimeout(autoCloseTimer);
        removeNotification(notificationElement);
    });
}

// Удаление уведомления с анимацией
function removeNotification(element) {
    element.style.transform = 'translateX(100%)';
    element.style.opacity = '0';

    setTimeout(() => {
        if (element.parentNode) {
            element.parentNode.removeChild(element);
        }
        activeNotifications = activeNotifications.filter(n => n !== element);

        // Удаляем контейнер, если уведомлений не осталось
        if (activeNotifications.length === 0 && notificationContainer) {
            notificationContainer.remove();
            notificationContainer = null;
        }
    }, 300);
}

// Проверка, находится ли пользователь в этом чате
function isUserInChat(conversationId) {
    const isChatPage = window.location.pathname.includes('/chat/');
    return isChatPage &&
        window.currentConversationId &&
        window.currentConversationId === conversationId;
}

// Звук уведомления
function playNotificationSound() {
    try {
        notificationSound.currentTime = 0; // Перематываем на начало
        notificationSound.play().catch(e => console.log("Браузер заблокировал звук"));
    } catch (e) {
        console.error("Ошибка воспроизведения звука:", e);
    }
}

// Отслеживаем онлайн-статус
window.addEventListener('online', () => {
    if (!isOnline) connectWebSocket();
});

window.addEventListener('offline', () => {
    isOnline = false;
});

// Запуск при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    connectWebSocket();
});