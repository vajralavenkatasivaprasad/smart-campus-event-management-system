// auth.js - Handle auth state UI
function initAuthUI() {
    const user = getUser();
    const authButtons = document.getElementById('authButtons');
    const userMenu = document.getElementById('userMenu');
    const notifBell = document.getElementById('notifBell');
    const userAvatar = document.getElementById('userAvatar');

    if (user && isLoggedIn()) {
        if (authButtons) authButtons.classList.add('hidden');
        if (userMenu) { userMenu.classList.remove('hidden'); }
        if (notifBell) notifBell.classList.remove('hidden');
        if (userAvatar) userAvatar.textContent = user.name ? user.name[0].toUpperCase() : 'U';
        loadUnreadCount();
    } else {
        if (authButtons) authButtons.classList.remove('hidden');
        if (userMenu) userMenu.classList.add('hidden');
        if (notifBell) notifBell.classList.add('hidden');
    }
}

async function loadUnreadCount() {
    try {
        const res = await API.getUnreadCount();
        const badge = document.getElementById('notifCount');
        if (badge && res) badge.textContent = res.count || 0;
    } catch(e) {}
}

async function toggleNotifications() {
    const panel = document.getElementById('notifPanel');
    if (!panel) return;
    panel.classList.toggle('hidden');
    if (!panel.classList.contains('hidden')) {
        const list = document.getElementById('notifList');
        list.innerHTML = '<div class="loading-spinner"><div class="spinner"></div></div>';
        try {
            const notifs = await API.getNotifications();
            if (!notifs || notifs.length === 0) {
                list.innerHTML = '<p class="empty-state">No notifications yet</p>';
                return;
            }
            list.innerHTML = notifs.map(n => `
                <div class="notif-item ${!n.read ? 'unread' : ''}" onclick="markNotifRead(${n.id})">
                    <h5>${n.title}</h5>
                    <p>${n.message}</p>
                    <small style="color:var(--gray-500)">${formatDate(n.createdAt)}</small>
                </div>`).join('');
        } catch(e) { list.innerHTML = '<p class="empty-state">Could not load notifications</p>'; }
    }
}

async function markNotifRead(id) {
    await API.markNotifRead(id);
    loadUnreadCount();
}

// Navbar scroll effect
function initNavbar() {
    const navbar = document.getElementById('navbar');
    if (!navbar) return;
    window.addEventListener('scroll', () => {
        if (window.scrollY > 20) navbar.classList.add('scrolled');
        else navbar.classList.remove('scrolled');
    });
    const toggle = document.getElementById('navToggle');
    const navLinks = document.getElementById('navLinks');
    if (toggle && navLinks) {
        toggle.addEventListener('click', () => navLinks.classList.toggle('open'));
    }
}

document.addEventListener('DOMContentLoaded', () => {
    initAuthUI();
    initNavbar();
});
