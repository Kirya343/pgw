function updateFileName() {
    const input = document.getElementById('image');
    const fileName = input.files.length > 0 ? input.files[0].name : '#{listing.editing.image.file.not.selected}';
    const fileNameSpan = document.querySelector('.file-name');
    fileNameSpan.textContent = fileName;
}