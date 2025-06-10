document.addEventListener('DOMContentLoaded', function () {
    const mainImage = document.getElementById('mainImageView');
    const thumbnails = document.querySelectorAll('.thumbnail-img');
    const prevArrow = document.querySelector('.prev-arrow');
    const nextArrow = document.querySelector('.next-arrow');
    const modal = document.getElementById('fullscreen-modal');
    const modalImage = document.getElementById('fullscreen-image');
    const closeModal = document.getElementById('close-modal');
    const modalPrev = document.querySelector('.modal-prev');
    const modalNext = document.querySelector('.modal-next');

    let currentIndex = 0;
    let isAnimating = false;

    // Обновление основного изображения и миниатюр
    function updateMainImage() {
        if (thumbnails.length === 0 || isAnimating) return;

        const currentThumb = thumbnails[currentIndex];
        const newSrc = currentThumb.getAttribute('src');
        mainImage.setAttribute('src', newSrc);

        thumbnails.forEach(thumb => thumb.classList.remove('active'));
        currentThumb.classList.add('active');
    }

    // Инициализация
    function initGallery() {
        thumbnails.forEach((thumb, index) => {
            thumb.addEventListener('click', function () {
                currentIndex = index;
                updateMainImage();
                openFullscreen();
            });
        });

        if (thumbnails.length > 0) {
            updateMainImage(); // Установить начальное изображение
        }
    }

    // Открытие модального окна
    function openFullscreen() {
        modalImage.setAttribute('src', thumbnails[currentIndex].getAttribute('src'));
        modal.classList.add('show');
    }

    // Закрытие модального окна
    closeModal.addEventListener('click', function () {
        modal.classList.remove('show');
    });

    modal.addEventListener('click', function (e) {
        if (e.target === modal) {
            modal.classList.remove('show');
        }
    });

    // Обработка стрелок (влево/вправо)
    function changeImage(offset) {
        if (isAnimating || thumbnails.length === 0) return;

        currentIndex = (currentIndex + offset + thumbnails.length) % thumbnails.length;
        updateMainImage();
    }

    prevArrow.addEventListener('click', () => changeImage(-1));
    nextArrow.addEventListener('click', () => changeImage(1));

    mainImage.addEventListener('click', function () {
        openFullscreen();
    });

    function changeModalImage(offset) {
        if (isAnimating || thumbnails.length === 0) return;

        isAnimating = true;
        modalImage.classList.add('changing');

        setTimeout(() => {
            currentIndex = (currentIndex + offset + thumbnails.length) % thumbnails.length;
            modalImage.setAttribute('src', thumbnails[currentIndex].getAttribute('src'));

            setTimeout(() => {
                modalImage.classList.remove('changing');
                isAnimating = false;
            }, 50);
        }, 150);
    }

    modalPrev.addEventListener('click', () => changeModalImage(-1));
    modalNext.addEventListener('click', () => changeModalImage(1));

    initGallery();
});