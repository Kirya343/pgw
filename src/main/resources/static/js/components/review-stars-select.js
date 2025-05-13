// выбор звёздочек рейтинга для отзыва
document.querySelectorAll('.star').forEach(function(star) {
    star.addEventListener('mouseover', function() {
        let value = parseInt(star.getAttribute('data-value'));
        document.querySelectorAll('.star').forEach(function(s) {
            s.classList.remove('selected');
            if (parseInt(s.getAttribute('data-value')) <= value) {
                s.classList.add('selected');
            }
        });
    });

    star.addEventListener('click', function() {
        let value = parseInt(star.getAttribute('data-value'));
        document.getElementById('rating').value = value;
    });

    star.addEventListener('mouseout', function() {
        let value = document.getElementById('rating').value;
        document.querySelectorAll('.star').forEach(function(s) {
            s.classList.remove('selected');
            if (parseInt(s.getAttribute('data-value')) <= value) {
                s.classList.add('selected');
            }
        });
    });
});