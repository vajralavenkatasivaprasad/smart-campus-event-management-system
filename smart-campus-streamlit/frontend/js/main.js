// main.js - Homepage logic

document.addEventListener('DOMContentLoaded', async () => {
    loadUpcomingEvents();
    createParticles();
});

async function loadUpcomingEvents() {
    const container = document.getElementById('upcomingEvents');
    if (!container) return;
    try {
        const res = await API.getEvents({ page: 0, size: 6 });
        if (!res || !res.events || res.events.length === 0) {
            container.innerHTML = '<p class="empty-state" style="grid-column:1/-1">No upcoming events found.</p>';
            return;
        }
        container.innerHTML = res.events.map(e => createEventCard(e)).join('');
        document.getElementById('statEvents').textContent = (res.totalElements || 0) + '+';
    } catch(e) {
        container.innerHTML = '<p class="empty-state" style="grid-column:1/-1">Could not load events. Make sure the backend is running.</p>';
    }
}

function createParticles() {
    const container = document.getElementById('particles');
    if (!container) return;
    for (let i = 0; i < 15; i++) {
        const p = document.createElement('div');
        p.style.cssText = `
            position:absolute;
            width:${Math.random()*8+4}px;
            height:${Math.random()*8+4}px;
            background:rgba(108,99,255,${Math.random()*0.3+0.1});
            border-radius:50%;
            top:${Math.random()*100}%;
            left:${Math.random()*100}%;
            animation:float ${Math.random()*4+3}s ease-in-out infinite;
            animation-delay:${Math.random()*3}s;
        `;
        container.appendChild(p);
    }
}
