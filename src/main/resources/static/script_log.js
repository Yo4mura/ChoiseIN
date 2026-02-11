const userEmail = /*[[${userEmail}]]*/ null;
if (userEmail) {
    localStorage.setItem('userEmail', userEmail);
    window.location.href = '/'; // переход на страницу теста
}
function switchToLogin() {
    document.getElementById('loginForm').classList.remove('hidden-form');
    document.getElementById('registerForm').classList.add('hidden-form');
 }
function switchToRegister() {
    document.getElementById('loginForm').classList.add('hidden-form');
    document.getElementById('registerForm').classList.remove('hidden-form');
}
function togglePassword(id) {
    const input = document.getElementById(id);
    input.type = input.type === 'password' ? 'text' : 'password';
}
function handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;
    console.log('Login:', { email, password });
    alert('Вход успешен!');
    localStorage.setItem('userEmail', email);
    window.location.href = '/';
}
function handleRegister(e) {
    e.preventDefault();
    const name = document.getElementById('registerName').value;
    const email = document.getElementById('registerEmail').value;
    const password = document.getElementById('registerPassword').value;
    console.log('Register:', { name, email, password });
    alert('Регистрация успешна!');
    localStorage.setItem('userEmail', email);
    window.location.href = '/';
}
function handleSocial() {
    alert('Социальная авторизация скоро...');
}
