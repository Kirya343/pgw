document.addEventListener("DOMContentLoaded", function() {
    const loginLinks = document.querySelectorAll('a.login-link');
    loginLinks.forEach(link => {
        // Проверяем, нет ли уже ?redirect=
        if (!link.href.includes('redirect=')) {
            const currentUrl = window.location.pathname + window.location.search;
            const separator = link.href.includes('?') ? '&' : '?';
            link.href = link.href + separator + 'redirect=' + encodeURIComponent(currentUrl);
        }
    });
});