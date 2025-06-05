document.addEventListener('DOMContentLoaded', function() {
    const confirmDialog = document.getElementById('confirmDialog');
    const confirmAction = document.getElementById('confirmAction');
    const cancelAction = document.getElementById('cancelAction');
    let currentAction = null;

    document.querySelectorAll('[data-action]').forEach(button => {
        button.addEventListener('click', function() {
            currentAction = this.getAttribute('data-action');
            confirmDialog.style.display = 'block';
        });
    });

    confirmAction.addEventListener('click', function() {
        if (!currentAction) return;
        
        fetch(`/secure/security/${currentAction}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            }
        }).then(response => {
            if (response.ok) {
                window.location.reload();
            }
        });
    });

    cancelAction.addEventListener('click', function() {
        confirmDialog.style.display = 'none';
        currentAction = null;
    });
});