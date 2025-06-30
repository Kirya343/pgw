function loadSubcategories(parentId, targetSelectId) {
    console.log(`[loadSubcategories] Начало выполнения. parentId: ${parentId}, targetSelectId: ${targetSelectId}`);

    if (!parentId) {
        console.log('[loadSubcategories] parentId не указан, скрываем целевой select');
        document.getElementById(targetSelectId).style.display = 'none';
        document.getElementById(targetSelectId).disabled = true;
        return;
    }

    console.log(`[loadSubcategories] Запрашиваю подкатегории для parentId: ${parentId}`);
    fetch(`/api/categories/children/${parentId}`)
        .then(response => {
            console.log('[loadSubcategories] Получен ответ от /api/categories/children');
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(categories => {
            console.log('[loadSubcategories] Успешно получил категории:', categories);
            
            const select = document.getElementById(targetSelectId);
            console.log(`[loadSubcategories] Найден select: ${targetSelectId}`, select);
            
            // Очищаем и добавляем дефолтный option
            select.innerHTML = '<option value="" disabled selected>Выберите подкатегорию</option>';
            console.log('[loadSubcategories] Select очищен');
            
            // Добавляем категории в select
            categories.forEach((cat, index) => {
                console.log(`[loadSubcategories] Добавляю категорию ${index + 1}/${categories.length}:`, cat);
                const option = document.createElement('option');
                option.value = cat.id;
                option.textContent = cat.name;
                option.dataset.isLeaf = cat.leaf;
                select.appendChild(option);
            });
            
            // Показываем select
            select.style.display = 'block';
            select.disabled = false;
            console.log('[loadSubcategories] Select показан и активирован');
            
            // Скрываем следующие уровни
            const nextLevel = parseInt(targetSelectId.replace('subCategory', '')) + 1;
            const nextSelectId = `subCategory${nextLevel}`;
            const nextSelect = document.getElementById(nextSelectId);
            
            if (nextSelect) {
                console.log(`[loadSubcategories] Скрываю следующий уровень: ${nextSelectId}`);
                nextSelect.style.display = 'none';
                nextSelect.disabled = true;
            } else {
                console.log('[loadSubcategories] Следующий уровень не найден, возможно это последний уровень');
            }
            
            // Очищаем конечный выбор
            document.getElementById('finalCategoryId').value = '';
            document.getElementById('categoryError').style.display = 'none';
            console.log('[loadSubcategories] Конечный выбор и ошибки сброшены');

            // Загружаем путь категорий
            console.log(`[loadSubcategories] Запрашиваю путь для категории: ${parentId}`);
            return fetch(`/api/categories/path/${parentId}`);
        })
        .then(response => {
            console.log('[loadSubcategories] Получен ответ от /api/categories/path');
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(path => {
            console.log('[loadSubcategories] Получен путь категорий:', path);
            const pathText = path.map(c => c.name).join(' / ');
            document.getElementById('categoryPath').textContent = pathText;
            console.log(`[loadSubcategories] Установлен путь категорий: ${pathText}`);
        })
        .catch(error => {
            console.error('[loadSubcategories] Ошибка:', error);
            document.getElementById('categoryError').textContent = 'Ошибка загрузки категорий';
            document.getElementById('categoryError').style.display = 'block';
        })
        .finally(() => {
            console.log('[loadSubcategories] Завершение функции');
        });
}
// Проверка, что выбрана конечная категория
function checkLeafCategory(categoryId) {
    fetch(`/api/categories/is-leaf/${categoryId}`)
        .then(response => response.json())
        .then(isLeaf => {
            const errorElement = document.getElementById('categoryError');
            if (isLeaf) {
                document.getElementById('finalCategoryId').value = categoryId;
                errorElement.style.display = 'none';
            } else {
                document.getElementById('finalCategoryId').value = '';
                errorElement.textContent = 'Пожалуйста, выберите конечную категорию';
                errorElement.style.display = 'block';
            }
        });
}

// Валидация перед отправкой формы
document.getElementById('listingForm').addEventListener('submit', function(e) {
    if (!document.getElementById('finalCategoryId').value) {
        e.preventDefault();
        document.getElementById('categoryError').textContent = 'Пожалуйста, выберите конечную категорию';
        document.getElementById('categoryError').style.display = 'block';
        window.scrollTo({
            top: document.getElementById('rootCategory').offsetTop - 100,
            behavior: 'smooth'
        });
    }
});


