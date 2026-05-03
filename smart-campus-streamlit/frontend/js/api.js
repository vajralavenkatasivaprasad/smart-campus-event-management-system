// API Configuration
const API_BASE = 'http://localhost:8080/api';

const API = {
    // Auth
    register: (data) => post('/auth/register', data),
    verifyOtp: (data) => post('/auth/verify-otp', data),
    resendOtp: (data) => post('/auth/resend-otp', data),
    login: (data) => post('/auth/login', data),
    forgotPassword: (data) => post('/auth/forgot-password', data),
    resetPassword: (data) => post('/auth/reset-password', data),

    // Events
    getEvents: (params = {}) => get('/events', params),
    getEvent: (id) => get(`/events/${id}`),
    createEvent: (data) => post('/events', data, true),
    updateEvent: (id, data) => put(`/events/${id}`, data, true),
    deleteEvent: (id) => del(`/events/${id}`, true),
    registerEvent: (id) => post(`/events/${id}/register`, {}, true),
    unregisterEvent: (id) => del(`/events/${id}/unregister`, true),
    getEventRegistrations: (id) => get(`/events/${id}/registrations`, {}, true),
    submitFeedback: (id, data) => post(`/events/${id}/feedback`, data, true),
    getMyEvents: () => get('/events/my-events', {}, true),
    getMyOrganizedEvents: () => get('/events/my-organized', {}, true),

    // Venues
    getVenues: () => get('/venues'),
    getVenue: (id) => get(`/venues/${id}`),
    createVenue: (data) => post('/venues', data, true),
    updateVenue: (id, data) => put(`/venues/${id}`, data, true),
    deleteVenue: (id) => del(`/venues/${id}`, true),

    // Announcements
    getAnnouncements: (role) => get('/announcements', role ? { role } : {}),
    createAnnouncement: (data) => post('/announcements', data, true),
    deleteAnnouncement: (id) => del(`/announcements/${id}`, true),

    // Notifications
    getNotifications: () => get('/notifications', {}, true),
    getUnreadCount: () => get('/notifications/unread-count', {}, true),
    markNotifRead: (id) => put(`/notifications/${id}/read`, {}, true),

    // Chatbot
    sendChatMessage: (data) => post('/chatbot/message', data),

    // Admin
    getDashboardStats: () => get('/admin/dashboard-stats', {}, true),
    getAllUsers: () => get('/admin/users', {}, true),
    deleteUser: (id) => del(`/admin/users/${id}`, true),
    updateEventStatus: (id, status) => put(`/admin/events/${id}/status`, { status }, true),
};

// HTTP Helpers
async function get(endpoint, params = {}, auth = false) {
    const url = new URL(API_BASE + endpoint);
    Object.entries(params).forEach(([k, v]) => { if (v !== undefined && v !== '') url.searchParams.set(k, v); });
    const headers = { 'Content-Type': 'application/json' };
    if (auth) {
        const token = getToken();
        if (token) headers['Authorization'] = `Bearer ${token}`;
    }
    const res = await fetch(url, { headers });
    if (res.status === 401) { handleUnauthorized(); return null; }
    return res.json();
}

async function post(endpoint, data, auth = false) {
    const headers = { 'Content-Type': 'application/json' };
    if (auth) {
        const token = getToken();
        if (token) headers['Authorization'] = `Bearer ${token}`;
    }
    const res = await fetch(API_BASE + endpoint, {
        method: 'POST', headers, body: JSON.stringify(data)
    });
    return res.json();
}

async function put(endpoint, data, auth = false) {
    const headers = { 'Content-Type': 'application/json' };
    if (auth) {
        const token = getToken();
        if (token) headers['Authorization'] = `Bearer ${token}`;
    }
    const res = await fetch(API_BASE + endpoint, {
        method: 'PUT', headers, body: JSON.stringify(data)
    });
    return res.json();
}

async function del(endpoint, auth = false) {
    const headers = { 'Content-Type': 'application/json' };
    if (auth) {
        const token = getToken();
        if (token) headers['Authorization'] = `Bearer ${token}`;
    }
    const res = await fetch(API_BASE + endpoint, { method: 'DELETE', headers });
    return res.json();
}

function getToken() { return localStorage.getItem('ems_token'); }
function getUser() { const u = localStorage.getItem('ems_user'); return u ? JSON.parse(u) : null; }
function setAuth(token, user) {
    localStorage.setItem('ems_token', token);
    localStorage.setItem('ems_user', JSON.stringify(user));
}
function clearAuth() { localStorage.removeItem('ems_token'); localStorage.removeItem('ems_user'); }
function isLoggedIn() { return !!getToken(); }
function handleUnauthorized() { clearAuth(); window.location.href = '/pages/login.html'; }

// Utility functions
function formatDate(dateStr) {
    if (!dateStr) return 'TBA';
    return new Date(dateStr).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
}
function formatDateTime(dateStr) {
    if (!dateStr) return 'TBA';
    return new Date(dateStr).toLocaleString('en-IN', { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}
function categoryColor(cat) {
    const colors = { ACADEMIC: 'academic', CULTURAL: 'cultural', SPORTS: 'sports', WORKSHOP: 'workshop', SEMINAR: 'seminar', CONFERENCE: 'conference', SOCIAL: 'social' };
    return colors[cat] || 'other';
}
function showAlert(msg, type = 'success', container = 'alertContainer') {
    const el = document.getElementById(container);
    if (!el) return;
    const icons = { success: 'check-circle', error: 'exclamation-circle', info: 'info-circle', warning: 'exclamation-triangle' };
    el.innerHTML = `<div class="alert alert-${type}"><i class="fas fa-${icons[type]}"></i> ${msg}</div>`;
    if (type === 'success') setTimeout(() => { if (el) el.innerHTML = ''; }, 4000);
}
function showToast(msg, type = 'success') {
    const toast = document.createElement('div');
    toast.className = `alert alert-${type}`;
    toast.style.cssText = 'position:fixed;bottom:30px;left:50%;transform:translateX(-50%);z-index:9999;min-width:300px;animation:fadeIn 0.3s';
    toast.innerHTML = msg;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3500);
}
function logout() {
    clearAuth();
    window.location.href = '/index.html';
}
function createEventCard(event) {
    const isRegistered = false;
    return `
    <div class="event-card" onclick="window.location.href='/pages/event-detail.html?id=${event.id}'">
        <div class="event-card-img">
            ${event.bannerImage ? `<img src="${event.bannerImage}" alt="${event.title}">` : `<i class="fas fa-calendar-alt" style="font-size:48px;color:white;opacity:0.5"></i>`}
            <span class="event-category-badge">${event.category || 'OTHER'}</span>
            ${event.free || event.isFree ? '<span class="event-free-badge">FREE</span>' : ''}
        </div>
        <div class="event-card-body">
            <h3>${event.title}</h3>
            <div class="event-meta">
                <span><i class="fas fa-calendar"></i> ${formatDate(event.startDate)}</span>
                <span><i class="fas fa-clock"></i> ${new Date(event.startDate).toLocaleTimeString('en-IN', {hour:'2-digit',minute:'2-digit'})}</span>
                ${event.venue ? `<span><i class="fas fa-map-marker-alt"></i> ${event.venue.name}</span>` : ''}
            </div>
            <div class="event-card-footer">
                <span class="attendee-count"><i class="fas fa-users"></i> ${event.currentAttendees || 0}${event.maxAttendees ? `/${event.maxAttendees}` : ''} registered</span>
                <button class="btn btn-primary btn-sm" onclick="event.stopPropagation();window.location.href='/pages/event-detail.html?id=${event.id}'">View Details</button>
            </div>
        </div>
    </div>`;
}
