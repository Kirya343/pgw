(function() {
  const cookieName = "locale";

  // Функция для чтения куки по имени
  function getCookie(name) {
    const matches = document.cookie.match(new RegExp(
      "(?:^|; )" + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + "=([^;]*)"
    ));
    return matches ? decodeURIComponent(matches[1]) : undefined;
  }

  // Функция для установки куки
  function setCookie(name, value, days) {
    let expires = "";
    if (days) {
      const date = new Date();
      date.setTime(date.getTime() + (days*24*60*60*1000));
      expires = "; expires=" + date.toUTCString();
    }
    document.cookie = name + "=" + encodeURIComponent(value) + expires + "; path=/";
  }

  // Проверяем, есть ли куки
  const localeCookie = getCookie(cookieName);
  if (!localeCookie) {
    // Берём язык из браузера
    const browserLang = navigator.language || navigator.userLanguage || "en";
    setCookie(cookieName, browserLang, 30); // срок жизни 30 дней
    console.log(`Cookie "${cookieName}" установлена со значением: ${browserLang}`);
  } else {
    console.log(`Cookie "${cookieName}" уже установлена: ${localeCookie}`);
  }
})();