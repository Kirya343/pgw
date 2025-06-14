// Инициализация dropdown меню Bootstrap
document.addEventListener('DOMContentLoaded', function() {
    // Включаем работу dropdown меню
    var dropdownElementList = [].slice.call(document.querySelectorAll('.dropdown-toggle'))
    var dropdownList = dropdownElementList.map(function (dropdownToggleEl) {
        return new bootstrap.Dropdown(dropdownToggleEl)
    });
    
    // Закрываем меню после выбора пункта
    document.querySelectorAll('.dropdown-item').forEach(function(item) {
        item.addEventListener('click', function() {
            var dropdown = bootstrap.Dropdown.getInstance(document.querySelector('.dropdown-toggle'));
            dropdown.hide();
        });
    });
});