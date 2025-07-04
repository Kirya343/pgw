function openModal() {
    document.getElementById("adminModal").style.display = "flex";
}

function closeModal() {
    document.getElementById("adminModal").style.display = "none";
}

// Закрытие по клику вне окна
window.onclick = function(event) {
    const modal = document.getElementById("adminModal");
    if (event.target == modal) {
        modal.style.display = "none";
    }
}