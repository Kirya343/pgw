document.querySelectorAll('.avatar-option').forEach(option => {
    option.addEventListener('click', function() {
        // Удаляем выделение у всех вариантов
        document.querySelectorAll('.avatar-option').forEach(opt => {
            opt.classList.remove('selected');
        });

        // Добавляем выделение выбранному
        this.classList.add('selected');

        // Обновляем скрытое поле
        document.querySelector('input[name="avatarType"]').value =
            this.getAttribute('data-type');

        // Показываем поле загрузки только для "Моя"
        document.querySelector('.upload-controls').style.display =
            this.getAttribute('data-type') === 'uploaded' ? 'block' : 'none';
    });
});

// Выделяем текущий выбранный тип при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    const currentType = document.querySelector('input[name="avatarType"]').value;
    document.querySelector(`.avatar-option[data-type="${currentType}"]`).click();
    document.getElementById('myLangsContainer').innerHTML = `
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
});