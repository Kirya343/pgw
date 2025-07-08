function updateFileNames() {
    const input = document.getElementById('images');
    const fileNamesElement = document.getElementById('file-names');
    
    if (input.files.length > 0) {
        const names = Array.from(input.files).map(file => file.name).join(', ');
        fileNamesElement.textContent = names;
    } else {
        fileNamesElement.textContent = 'Файлы не выбраны';
    }
}

// Обработка загрузки новых изображений
function handleImageUpload(input) {
    if (input.files && input.files.length > 0) {
        Array.from(input.files).forEach(file => {
            const reader = new FileReader();
            
            reader.onload = function(e) {
                const tempId = 'temp-' + Date.now();
                const imageContainer = document.createElement('div');
                imageContainer.className = 'col-md-3 mb-3 image-item';
                imageContainer.innerHTML = `
                    <div class="card">
                        <img src="${e.target.result}" class="card-img-top img-thumbnail">
                        <div class="card-body p-2">
                            <div class="btn-group btn-group-sm w-100">
                                <button type="button" class="btn btn-outline-primary set-main-btn" 
                                        data-temp-id="${tempId}">
                                    Сделать основным
                                </button>
                                <button type="button" class="btn btn-outline-danger delete-temp-btn"
                                        data-temp-id="${tempId}">
                                    Удалить
                                </button>
                            </div>
                        </div>
                    </div>
                `;
                
                document.getElementById('imageGallery').appendChild(imageContainer);
                
                // Сохраняем файл в data-атрибуте
                const setMainBtn = imageContainer.querySelector('.set-main-btn');
                setMainBtn.file = file;
                setMainBtn.addEventListener('click', function() {
                    document.getElementById('mainImagePreview').src = e.target.result;
                    document.getElementById('mainImagePath').value = file.name;
                });
            };
            
            reader.readAsDataURL(file);
        });
    }
}

// Функция для загрузки и отображения изображения
function handleSingleImageUpload(input, existingImageUrl = null) {
    const imageGallery = document.getElementById('imageGallery');
    imageGallery.innerHTML = '';

    // Если передано существующее изображение - показываем его
    if (existingImageUrl) {
        const imageContainer = document.createElement('div');
        imageContainer.className = 'col-md-3 mb-3 image-item';
        imageContainer.innerHTML = `
            <div class="card">
                <img src="${existingImageUrl}" class="card-img-top img-thumbnail">
                <div class="card-body p-2">
                    <button type="button" class="btn btn-outline-danger btn-sm w-100 delete-temp-btn">
                        Удалить
                    </button>
                </div>
            </div>
        `;
        
        imageGallery.appendChild(imageContainer);
        
        imageContainer.querySelector('.delete-temp-btn').addEventListener('click', function() {
            imageGallery.removeChild(imageContainer);
            if (input) input.value = '';
            // Здесь можно добавить вызов API для удаления изображения на сервере
        });
    }

    // Обработка нового изображения (если загружают новое)
    if (input && input.files && input.files.length === 1) {
        const file = input.files[0];
        const reader = new FileReader();
        
        reader.onload = function(e) {
            // Очищаем предыдущее изображение
            imageGallery.innerHTML = '';
            
            const imageContainer = document.createElement('div');
            imageContainer.className = 'col-md-3 mb-3 image-item';
            imageContainer.innerHTML = `
                <div class="card">
                    <img src="${e.target.result}" class="card-img-top img-thumbnail">
                    <div class="card-body p-2">
                        <button type="button" class="btn btn-outline-danger btn-sm w-100 delete-temp-btn">
                            Удалить
                        </button>
                    </div>
                </div>
            `;
            
            imageGallery.appendChild(imageContainer);
            
            imageContainer.querySelector('.delete-temp-btn').addEventListener('click', function() {
                imageGallery.removeChild(imageContainer);
                input.value = '';
            });
        };
        
        reader.readAsDataURL(file);
    } else if (input && input.files && input.files.length > 1) {
        alert('Пожалуйста, выберите только одно изображение!');
        input.value = '';
    }
}

// При загрузке страницы редактирования
document.addEventListener('DOMContentLoaded', function() {
    const imageUrl = document.getElementById('existingImageUrl').value; // Предполагаем, что сервер передает URL
    
    if (imageUrl) {
        handleSingleImageUpload(null, imageUrl);
    }
    
    // Навешиваем обработчик на input file
    const uploadInput = document.getElementById('imageUpload');
    if (uploadInput) {
        uploadInput.addEventListener('change', function() {
            handleSingleImageUpload(this);
        });
    }
});

// Установка основного изображения для существующих
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('set-main-btn') && e.target.hasAttribute('data-image-path')) {
        const imagePath = e.target.getAttribute('data-image-path');
        document.getElementById('mainImagePreview').src = imagePath;
        document.getElementById('mainImagePath').value = imagePath;
    
        const tempId = e.target.getAttribute('data-temp-id');
        
        if (imagePath) {
            // Для существующих изображений
            document.getElementById('mainImagePreview').src = imagePath;
            document.getElementById('mainImagePath').value = imagePath;
        } else if (tempId) {
            // Для новых изображений
            const file = e.target.file;
            const reader = new FileReader();
            
            reader.onload = function(e) {
                document.getElementById('mainImagePreview').src = e.target.result;
                // Сохраняем файл как основное изображение
                document.getElementById('mainImagePath').file = file;
            };
            
            reader.readAsDataURL(file);
        }
    }
    
    // Удаление существующих изображений
    if (e.target.classList.contains('delete-btn')) {
        const imageId = e.target.getAttribute('data-image-id');
        const deletedInput = document.getElementById('deletedImages');
        const currentValue = deletedInput.value ? deletedInput.value.split(',') : [];
        currentValue.push(imageId);
        deletedInput.value = currentValue.join(',');
        
        e.target.closest('.image-item').remove();
    }
    
    // Удаление новых (еще не сохраненных) изображений
    if (e.target.classList.contains('delete-temp-btn')) {
        e.target.closest('.image-item').remove();
    }
});