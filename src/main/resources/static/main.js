const message = document.querySelector('#message');

function showMessage(text, isError = true) {
    if (!message) return;
    message.textContent = text;
    message.style.color = isError ? '#b91c1c' : '#15803d';
}

function displayCurrentToken(currentToken) {
    document.querySelector('#current-token').value = currentToken;
    document.querySelector('#token-panel').hidden = false;
}

async function readResponse(response) {
    const body = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(body.message || `Yêu cầu thất bại (${response.status})`);
    return body;
}

document.querySelectorAll('.tab').forEach((tab) => {
    tab.addEventListener('click', () => {
        document.querySelectorAll('.tab').forEach((item) => item.classList.toggle('active', item === tab));
        document.querySelectorAll('form').forEach((form) => { form.hidden = form.id !== tab.dataset.form; });
        showMessage('');
    });
});

document.querySelector('#signup-form')?.addEventListener('submit', async (event) => {
    event.preventDefault();
    const values = Object.fromEntries(new FormData(event.currentTarget));
    try {
        await readResponse(await fetch('/auth/signup', {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(values)
        }));
        document.querySelector('[data-form="login-form"]').click();
        document.querySelector('#login-form [name="email"]').value = values.email;
        showMessage('Tạo tài khoản thành công. Hãy đăng nhập.', false);
    } catch (error) { showMessage(error.message); }
});

document.querySelector('#login-form')?.addEventListener('submit', async (event) => {
    event.preventDefault();
    const values = Object.fromEntries(new FormData(event.currentTarget));
    try {
        const login = await readResponse(await fetch('/auth/login', {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(values)
        }));
        sessionStorage.setItem('jwt', login.token);
        window.location.assign('/profile.html');
    } catch (error) { showMessage(error.message); }
});

const token = sessionStorage.getItem('jwt');
const profile = document.querySelector('#profile');
if (profile) {
    if (!token) window.location.replace('/');
    else {
        fetch('/users/me', { headers: { Authorization: `Bearer ${token}` } })
            .then(readResponse)
            .then((user) => {
                document.querySelector('#loading').hidden = true;
                document.querySelector('#fullName').textContent = user.fullName;
                document.querySelector('#email').textContent = user.email;
                document.querySelector('#createdAt').textContent = new Date(user.createdAt).toLocaleString('vi-VN');
                profile.hidden = false;
                displayCurrentToken(token);
            })
            .catch((error) => {
                sessionStorage.removeItem('jwt');
                document.querySelector('#loading').hidden = true;
                const errorElement = document.querySelector('#error');
                errorElement.textContent = `${error.message}. Vui lòng đăng nhập lại.`;
                errorElement.hidden = false;
            });
    }
}

document.querySelector('#logout')?.addEventListener('click', () => {
    sessionStorage.removeItem('jwt');
    window.location.assign('/');
});

document.querySelector('#copy-token')?.addEventListener('click', async () => {
    try {
        await navigator.clipboard.writeText(sessionStorage.getItem('jwt'));
        document.querySelector('#copy-token').textContent = 'Đã sao chép';
    } catch {
        document.querySelector('#copy-token').textContent = 'Không thể sao chép';
    }
});
