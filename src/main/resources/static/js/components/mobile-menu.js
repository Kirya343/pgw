document.querySelectorAll('.navbar-toggler').forEach(select => {
    select.addEventListener('click', function() {
        document.querySelector('.navbar-collapse').classList.toggle('show');
    });
});