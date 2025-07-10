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

    hiddenInput.value = Array.from(selectedLanguages).join(',');
    }
});

function loadSubcategories(countryId, targetSelectId) {

    if (!countryId) {
        document.getElementById(targetSelectId).style.display = 'none';
        document.getElementById(targetSelectId).disabled = true;
        return;
    }

    fetch(`/api/locations/cities/${countryId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(locations => {
            
            const select = document.getElementById('city');
            console.log(select);

            // Очищаем и добавляем дефолтный option
            select.innerHTML = '<option value="" disabled selected>Выберите подкатегорию</option>';
            
            // Добавляем категории в select
            locations.forEach((loc) => {
                console.log(`[loadSubcategories] Добавляю локацию:`, loc);
                const option = document.createElement('option');
                option.value = loc.id;
                option.textContent = loc.name;
                select.appendChild(option);
            });
            
            // Показываем select
            select.style.display = 'block';
            select.disabled = false;
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

document.addEventListener('DOMContentLoaded', function () {
    const cityId = document.getElementById('locationId').value;
    if (cityId) {
        restoreCategoryPath(cityId);
    }
});

function restoreCategoryPath(cityId) {
    fetch(`/api/locations/getlocation/${cityId}`)
        .then(response => {
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            return response.json();
        })
        .then(loc => {
            const citySelect = document.getElementById('city');
            const countrySelect = document.getElementById('country');

            if (loc.city) {
                // Это город — сначала отобразим страну
                fetch(`/api/locations/getlocation/${loc.countryId}`)
                    .then(response => {
                        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                        return response.json();
                    })
                    .then(country => {
                        // Отрисовываем страну
                        countrySelect.innerHTML = '';
                        const countryOption = document.createElement('option');
                        countryOption.value = country.id;
                        countryOption.textContent = country.name;
                        countryOption.selected = true;
                        countrySelect.appendChild(countryOption);
                    });

                // Отрисовываем город
                citySelect.innerHTML = '';
                const cityOption = document.createElement('option');
                cityOption.value = loc.id;
                cityOption.textContent = loc.name;
                cityOption.selected = true;
                citySelect.appendChild(cityOption);

                citySelect.style.display = 'block';
                citySelect.disabled = false;

            } else {
                // Это страна — города скрываем
                countrySelect.innerHTML = '';
                const countryOption = document.createElement('option');
                countryOption.value = loc.id;
                countryOption.textContent = loc.name;
                countryOption.selected = true;
                countrySelect.appendChild(countryOption);

                citySelect.innerHTML = '<option value="" disabled selected>Выберите подкатегорию</option>';
                citySelect.style.display = 'none';
                citySelect.disabled = true;
            }

            // Обновляем скрытое поле
            document.getElementById('locationId').value = loc.id;
        })
        .catch(err => {
            console.error('[restoreLocations] Ошибка при восстановлении пути:', err);
        });
}