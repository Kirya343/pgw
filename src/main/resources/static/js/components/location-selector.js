function loadSities(countryId, targetSelectId) {

    if (!countryId) {
        document.getElementById(targetSelectId).style.display = 'none';
        document.getElementById(targetSelectId).disabled = true;
        return;
    }

    selectCategory(countryId)

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
            console.error('[loadSubcategories] Получена ошибка:', error);
        })
}

document.addEventListener('DOMContentLoaded', function () {
    const cityId = document.getElementById('locationId').value;
    if (cityId) {
        restoreLocationPath(cityId);
    }
});

function selectCategory(value) {
    document.getElementById('locationId').value = value;
}

function restoreLocationPath(cityId) {
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
                        
                        const countryOption = Array.from(countrySelect.options).find(
                            option => option.value == country.id
                        );
                        
                        if (countryOption) {
                            // Если нашли — выбираем её
                            countryOption.selected = true;
                        } else {
                            console.warn(`Страна с id ${country.id} не найдена в списке.`);
                        }
                    });

                citySelect.innerHTML = '';

                fetch(`/api/locations/cities/${loc.countryId}`)
                    .then(response => {
                        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                        return response.json();
                    })
                    .then(cities => {

                        // Отрисовываем остальные Города
                        cities.forEach((city) => {
                            if (loc.id != city.id) {
                                console.log(`[loadSubcategories] Добавляю локацию:`, city.name);
                                const cityOption = document.createElement('option');
                                cityOption.value = city.id;
                                cityOption.textContent = city.name;
                                citySelect.appendChild(cityOption);
                            }
                        });
                    });

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
        })
        .catch(err => {
            console.error('[restoreLocations] Ошибка при восстановлении пути:', err);
        });
}