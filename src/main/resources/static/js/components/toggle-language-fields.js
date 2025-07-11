const addLangBtn = document.getElementById('addLangBtn');
const languageSelectorContainer = document.getElementById('languageSelectorContainer');
const languageSelect = document.getElementById('languageSelect');
const languageFields = document.getElementById('languageFields');
const langLabel = document.getElementById('langLabel');
const langLabelDesc = document.getElementById('langLabelDesc');
const titleInput = document.getElementById('titleInput');
const descriptionInput = document.getElementById('descriptionInput');
const okBtn = document.getElementById('okBtn');
const languageIndicators = document.getElementById('languageIndicators');

const listingCardLabel = document.getElementById('listingCardLabel');
const listingCardDesc = document.getElementById('listingCardDesc');

// Сохраняем введённые данные по языкам
const languagesData = {};

let currentLanguage = null;
let isFormOpen = false;

addLangBtn.addEventListener('click', () => {
    if (!isFormOpen) {
        // Открываем селект
        languageSelectorContainer.style.display = 'block';
        languageSelect.value = '';
        languageFields.style.display = 'none';
        isFormOpen = true;
    } else {
        // Если форма открыта и язык выбран, считаем что это "ОК"
        if (currentLanguage) {
        saveLanguageData();
        closeForm();
        } else {
        alert('Выберите язык перед добавлением');
        }
    }
});

languageSelect.addEventListener('change', () => {
    currentLanguage = languageSelect.value;
    langLabel.textContent = languageSelect.options[languageSelect.selectedIndex].text;
    langLabelDesc.textContent = langLabel.textContent;
    titleInput.value = languagesData[currentLanguage]?.title || '';
    descriptionInput.value = languagesData[currentLanguage]?.description || '';
    languageFields.style.display = 'block';
});

okBtn.addEventListener('click', () => {
    saveLanguageData();
    closeForm();
});

function loadTranslations(translations) {

    if (!translations) return;

    // Копируем данные в languagesData
    Object.keys(translations).forEach(lang => {
        languagesData[lang] = {
            title: translations[lang].title || '',
            description: translations[lang].description || ''
        };

        // Обновляем индикаторы для каждого языка
        updateIndicator(lang, languagesData[lang].title, languagesData[lang].description);
    });
}

function saveLanguageData() {
    if (!currentLanguage) return;

    const title = titleInput.value.trim();
    const description = descriptionInput.value.trim();

    listingCardLabel.textContent = titleInput.value.trim();
    listingCardDesc.textContent = descriptionInput.value.trim();

    languagesData[currentLanguage] = { title, description };

    updateIndicator(currentLanguage, title, description);
}

function closeForm() {
    languageSelectorContainer.style.display = 'none';
    languageFields.style.display = 'none';
    isFormOpen = false;
    currentLanguage = null;
}

function updateIndicator(lang, title, description) {
    let indicator = document.querySelector(`.indicator[data-lang="${lang}"]`);
    if (!indicator) {
        indicator = document.createElement('div');
        indicator.classList.add('indicator');
        indicator.setAttribute('data-lang', lang);
        indicator.style.position = 'relative';

        const langText = document.createElement('span');
        langText.classList.add('lang-text');
        indicator.appendChild(langText);

        const deleteCross = document.createElement('span');
        deleteCross.textContent = '×';
        deleteCross.classList.add('delete-cross');

        // При клике на крестик — удаляем язык и сам индикатор
        deleteCross.addEventListener('click', (e) => {
            e.stopPropagation(); // чтобы не сработал клик по индикатору
            delete languagesData[lang];
            indicator.remove();
            // Если редактировался этот язык, закроем форму
            if (currentLanguage === lang) {
                closeForm();
            }
        });


        indicator.appendChild(deleteCross);
        languageIndicators.appendChild(indicator);

        indicator.addEventListener('click', (e) => {
            if (e.target.classList.contains('delete-cross')) return;
            openLanguageForm(lang);
        });
    }

    const langText = indicator.querySelector('.lang-text');
    if (langText) {
        langText.textContent = lang.toUpperCase();
    }

    if (title && description) {
        indicator.classList.remove('yellow');
        indicator.classList.add('green');
    } else {
        indicator.classList.remove('green');
        indicator.classList.add('yellow');
    }
}

// Перед отправкой формы — соберём все данные в скрытые поля
// Пример (надо подкорректировать под твой post-mapping):
function prepareFormDataForSubmit() {
    const form = document.getElementById('listingForm');
    if (!form) {
        console.error('Form not found!');
        return;
    }
    // Удаляем старые скрытые поля
    form.querySelectorAll('.lang-hidden-input').forEach(el => el.remove());

    Object.entries(languagesData).forEach(([lang, data]) => {
        const inputTitle = document.createElement('input');
        inputTitle.type = 'hidden';
        inputTitle.name = `translations[${lang}].title`;
        inputTitle.value = data.title;
        inputTitle.classList.add('lang-hidden-input');
        form.appendChild(inputTitle);

        const inputDesc = document.createElement('input');
        inputDesc.type = 'hidden';
        inputDesc.name = `translations[${lang}].description`;
        inputDesc.value = data.description;
        inputDesc.classList.add('lang-hidden-input');
        form.appendChild(inputDesc);
    });
}


function openLanguageForm(lang) {
    currentLanguage = lang;
    languageSelectorContainer.style.display = 'block';
    languageSelectorContainer.style.сursor = 'pointer';
    languageSelect.value = lang;
    langLabel.textContent = languageSelect.options[languageSelect.selectedIndex].text;
    langLabelDesc.textContent = langLabel.textContent;
    titleInput.value = languagesData[lang]?.title || '';
    descriptionInput.value = languagesData[lang]?.description || '';
    languageFields.style.display = 'block';
    isFormOpen = true;
}

document.getElementById('listingForm').addEventListener('submit', function (e) {
    console.log('Form submit started');
    prepareFormDataForSubmit();
    console.log('Hidden inputs added:', document.querySelectorAll('.lang-hidden-input').length);
});