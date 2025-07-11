function loadSubcategories(parentId, targetSelectId) {

    if (!parentId) {
        document.getElementById(targetSelectId).style.display = 'none';
        document.getElementById(targetSelectId).disabled = true;
        return;
    }

    fetch(`/api/categories/children/${parentId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(categories => {
            
            const select = document.getElementById(targetSelectId);
            
            // Очищаем и добавляем дефолтный option
            select.innerHTML = '<option value="" disabled selected>Выберите подкатегорию</option>';
            
            // Добавляем категории в select
            categories.forEach((cat, index) => {
                console.log(`[loadSubcategories] Добавляю категорию ${index + 1}/${categories.length}:`, cat);
                const option = document.createElement('option');
                option.value = cat.id;
                option.textContent = cat.translate;
                option.dataset.isLeaf = cat.leaf;
                select.appendChild(option);
            });
            
            // Показываем select
            select.style.display = 'block';
            select.disabled = false;
            
            // Скрываем следующие уровни
            const nextLevel = parseInt(targetSelectId.replace('subCategory', '')) + 1;
            const nextSelectId = `subCategory${nextLevel}`;
            const nextSelect = document.getElementById(nextSelectId);
            
            if (nextSelect) {
                nextSelect.style.display = 'none';
                nextSelect.disabled = true;
            } else {
                console.log('[loadSubcategories] Получена ошибка');
            }
            
            // Очищаем конечный выбор
            document.getElementById('finalCategoryId').value = '';
            document.getElementById('categoryError').style.display = 'none';

            // Загружаем путь категорий
            return fetch(`/api/categories/path/${parentId}`);
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .catch(error => {
            document.getElementById('categoryError').textContent = 'Ошибка загрузки категорий';
            document.getElementById('categoryError').style.display = 'block';
            console.error('[loadSubcategories] Получена ошибка:', error);
        })
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

function checkAndLoad(parentId, targetSelectId) {
    fetch(`/api/categories/is-leaf/${parentId}`)
        .then(response => response.json())
        .then(isLeaf => {
            if (!isLeaf) {
                loadSubcategories(parentId, targetSelectId)
            } else {
                checkLeafCategory(parentId)
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

document.addEventListener('DOMContentLoaded', function () {
    const categoryId = document.getElementById('finalCategoryId').value;
    if (categoryId) {
        restoreCategoryPath(categoryId);
    }
});

function restoreCategoryPath(categoryId) {

    fetch(`/api/categories/path/${categoryId}`)
        .then(response => {
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            return response.json();
        })
        .then(path => {
            const selectIds = ['rootCategory', 'subCategory1', 'subCategory2', 'subCategory3'];

            // Проходимся по каждой категории в пути и выставляем соответствующий select
            path.forEach((category, index) => {

                if (index >= selectIds.length) {
                    return;
                }

                const select = document.getElementById(selectIds[index]);
                if (!select) {
                    return;
                }

                // Показываем select и делаем его активным
                select.style.display = 'block';
                select.disabled = false;

                if (index > 0) {
                    const parentCategoryId = path[index - 1].id;

                    return fetch(`/api/categories/children/${parentCategoryId}`)
                        .then(res => res.json())
                        .then(children => {
                            // Очищаем и заполняем select
                            select.innerHTML = '<option value="" disabled>Выберите подкатегорию</option>';
                            children.forEach(child => {
                                const option = document.createElement('option');
                                option.value = child.id;
                                option.textContent = child.translate;
                                option.selected = child.id === category.id;
                                select.appendChild(option);
                            });
                        })
                        .catch(err => {
                            console.error(`Ошибка при загрузке подкатегорий для ${parentCategoryId}:`, err);
                        });
                } else {
                    // Корневая категория уже должна быть в select'е
                    const option = select.querySelector(`option[value="${category.id}"]`);
                    if (option) {
                        option.selected = true;
                    } else {
                        console.warn(`Опция с id=${category.id} не найдена в корневом select'е`);
                    }
                }
            });

            // Устанавливаем финальную категорию и путь
            const finalCategory = path[path.length - 1];
            document.getElementById('finalCategoryId').value = finalCategory.id;
        })
        .catch(err => {
            console.error('[restoreCategoryPath] Ошибка при восстановлении пути:', err);
            document.getElementById('categoryError').textContent = 'Не удалось восстановить путь категории';
            document.getElementById('categoryError').style.display = 'block';
        });
}