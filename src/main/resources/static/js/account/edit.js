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

    hiddenInput.value = Array.from(userLanguages).join(',');
});

const container = document.getElementById('myLangsContainer');
const hiddenInput = document.getElementById('selectedLanguagesInput');
const selectedLanguages = new Set();

const userLanguages = document.getElementById('selectedLanguagesInput').value.replace(/[\[\]\s]/g, '').split(',');
if (userLanguages) {
    userLanguages.forEach(lang => {
        
    const btn = container.querySelector(`.lang-select-btn[name="${lang}"]`);
    if (btn) {
        btn.classList.add('active');
        selectedLanguages.add(lang);
    }
    });
}

container.addEventListener('click', (e) => {
    if (e.target.classList.contains('lang-select-btn')) {
    const lang = e.target.name;
    e.target.classList.toggle('active');

    if (selectedLanguages.has(lang)) {
        selectedLanguages.delete(lang);
    } else {
        selectedLanguages.add(lang);
    }

    console.log(selectedLanguages)

    hiddenInput.value = Array.from(selectedLanguages).join(',');
    }
});