function handleServicesClick(event) {
    const params = new URLSearchParams(window.location.search);
    const currentCategory = params.get("category");
    
    // Если уже выбрана категория "services", сбрасываем фильтр
    if (currentCategory === 'services') {
        event.preventDefault();
        loadCatalogWithoutCategory();
        return;
    }
    
    // Иначе позволим стандартному поведению (открытие dropdown)
}

function loadCatalogWithoutCategory() {
    const params = new URLSearchParams(window.location.search);
    
    // Удаляем параметр категории
    params.delete("category");
    
    // Сохраняем другие параметры
    const searchQuery = params.get("searchQuery") || "";
    const hasReviews = params.get("hasReviews") === "true";
    const sortBy = params.get("sortBy") || "date";
    
    // Загружаем каталог без категории
    fetch(`/catalog/sort?${params.toString()}`, {
        headers: {
            "X-Requested-With": "XMLHttpRequest"
        }
    })
    .then(response => response.text())
    .then(html => {
        document.querySelector("#catalog-content").innerHTML = html;
        // Обновляем URL без перезагрузки страницы
        window.history.pushState({}, "", `/catalog?${params.toString()}`);
        
        // Сбрасываем активное состояние категорий
        document.querySelectorAll('.nav-link').forEach(link => {
            link.classList.remove('active');
        });
    })
    .catch(error => console.error("Ошибка:", error));
}

// Обновляем функцию loadSortedCatalog для обработки сброса категории
function loadSortedCatalog() {
    const params = new URLSearchParams(window.location.search);
    const category = params.get("category") || "all";
    // ... остальной код остается прежним ...
}

// Обработчик для кнопки "Все услуги"
document.querySelectorAll('.dropdown-item[th\\:href*="category=services"]').forEach(item => {
    item.addEventListener('click', function(e) {
        e.preventDefault();
        const url = new URL(this.href);
        const params = new URLSearchParams(url.search);
        
        // Загружаем контент через AJAX
        fetch(`/catalog/sort?${params.toString()}`, {
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        })
        .then(response => response.text())
        .then(html => {
            document.querySelector("#catalog-content").innerHTML = html;
            // Обновляем URL
            window.history.pushState({}, "", this.href);
        })
        .catch(error => console.error("Ошибка:", error));
    });
});
