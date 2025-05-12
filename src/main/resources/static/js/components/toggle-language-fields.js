function toggleLanguageFields() {
    // Читаем состояния переключателей
    const russianChecked = document.getElementById('audienceRussian').checked;
    const finnishChecked = document.getElementById('audienceFinnish').checked;
    const englishChecked = document.getElementById('audienceEnglish').checked;

    // Управляем отображением полей
    document.getElementById('titleRuField').style.display = russianChecked ? 'block' : 'none';
    document.getElementById('descriptionRuField').style.display = russianChecked ? 'block' : 'none';

    document.getElementById('titleFiField').style.display = finnishChecked ? 'block' : 'none';
    document.getElementById('descriptionFiField').style.display = finnishChecked ? 'block' : 'none';

    document.getElementById('titleEnField').style.display = englishChecked ? 'block' : 'none';
    document.getElementById('descriptionEnField').style.display = englishChecked ? 'block' : 'none';
}