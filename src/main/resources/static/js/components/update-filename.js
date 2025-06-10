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