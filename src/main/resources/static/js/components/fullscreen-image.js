// Получаем все изображения, по которым будет открываться полный экран
const images = document.querySelectorAll('.clickable-image');
const modal = document.getElementById('fullscreen-modal');
const fullscreenImage = document.getElementById('fullscreen-image');
const closeModal = document.getElementById('close-modal');

// Функция для открытия модального окна
images.forEach(image => {
    image.addEventListener('click', function() {
        const imageUrl = this.getAttribute('data-fullscreen');
        fullscreenImage.src = imageUrl;
        modal.classList.add('show');
    });
});

// Закрытие модального окна
closeModal.addEventListener('click', function() {
    modal.classList.remove('show');
});

// Закрытие модального окна при клике за его пределами
modal.addEventListener('click', function(event) {
    if (event.target === modal) {
        modal.classList.remove('show');
    }
});