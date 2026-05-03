import streamlit as st
import sys, os
sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))
from api_utils import get

def show():
    user = st.session_state.user
    st.markdown(f'<div class="main-header"><h1>🏠 Dashboard</h1><p>Welcome back, {user["name"]}!</p></div>',
                unsafe_allow_html=True)

    # Stats row
    col1, col2, col3, col4 = st.columns(4)

    events_data, _ = get("/events?size=100")
    total_events = events_data.get("totalElements", 0) if events_data else 0

    my_events_data, _ = get("/events/my-events")
    my_count = len(my_events_data) if my_events_data else 0

    venues_data, _ = get("/venues")
    venue_count = len(venues_data) if venues_data else 0

    ann_data, _ = get("/announcements")
    ann_count = len(ann_data) if ann_data else 0

    col1.metric("📅 Total Events", total_events)
    col2.metric("🗓️ My Registrations", my_count)
    col3.metric("📍 Venues", venue_count)
    col4.metric("📢 Announcements", ann_count)

    st.divider()

    # Recent events
    col_e, col_a = st.columns([3, 2])

    with col_e:
        st.subheader("🔥 Upcoming Events")
        if events_data:
            for evt in events_data.get("events", [])[:5]:
                with st.container():
                    st.markdown(f"**{evt['title']}**")
                    cat = evt.get("category", "OTHER")
                    status = evt.get("status", "PUBLISHED")
                    st.caption(f"📂 {cat}  |  📊 {status}  |  👥 {evt.get('currentAttendees',0)}/{evt.get('maxAttendees','∞')}")
                    st.divider()

    with col_a:
        st.subheader("📢 Latest Announcements")
        if ann_data:
            for ann in ann_data[:4]:
                with st.container():
                    pin = "📌 " if ann.get("isPinned") else ""
                    st.markdown(f"**{pin}{ann['title']}**")
                    st.caption(ann["content"][:100] + ("..." if len(ann["content"]) > 100 else ""))
                    st.divider()

    # Admin stats
    if user.get("role") == "ADMIN":
        st.subheader("⚙️ Admin Overview")
        stats, err = get("/admin/dashboard-stats")
        if stats:
            c1, c2, c3, c4 = st.columns(4)
            c1.metric("👥 Total Users",         stats.get("totalUsers", 0))
            c2.metric("📅 Published Events",    stats.get("publishedEvents", 0))
            c3.metric("✅ Completed Events",    stats.get("completedEvents", 0))
            c4.metric("🎟️ Total Registrations", stats.get("totalRegistrations", 0))
