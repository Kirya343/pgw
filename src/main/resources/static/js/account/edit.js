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
});