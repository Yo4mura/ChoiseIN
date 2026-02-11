function logout() {
    localStorage.removeItem('userEmail');
    window.location.href = '/log';
}

// Получаем email из localStorage для отправки запросов
function getUserEmail() {
    return localStorage.getItem('userEmail');
}

// Проверяем авторизацию и перенаправляем на профиль с email
document.addEventListener('DOMContentLoaded', function() {
    const email = getUserEmail();
    if (!email) {
        window.location.href = '/log';
    } else {
        // Добавляем email к URL если его нет
        const urlParams = new URLSearchParams(window.location.search);
        if (!urlParams.has('email')) {
            window.location.href = '/profile?email=' + email;
        }
    }
    
    // Анимация графиков при загрузке
    const chartBars = document.querySelectorAll('.chart-bar-fill');
    chartBars.forEach(bar => {
        const width = bar.style.width;
        bar.style.width = '0';
        setTimeout(() => {
            bar.style.width = width;
        }, 100);
    });
});

