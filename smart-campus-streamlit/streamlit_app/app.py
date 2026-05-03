"""
Smart Campus Event Management System — Streamlit Frontend
Run: streamlit run app.py
"""
import streamlit as st

st.set_page_config(
    page_title="Smart Campus EMS",
    page_icon="🎓",
    layout="wide",
    initial_sidebar_state="expanded",
)

# ── Session defaults ──────────────────────────────────────────
for key, val in {
    "token": None,
    "user": None,
    "api_base": "http://localhost:8080/api",
}.items():
    if key not in st.session_state:
        st.session_state[key] = val

# ── Custom CSS ────────────────────────────────────────────────
st.markdown("""
<style>
    [data-testid="stSidebar"] { background: linear-gradient(180deg,#1a1a2e 0%,#16213e 100%); }
    [data-testid="stSidebar"] * { color: #e0e0e0 !important; }
    .main-header { background: linear-gradient(135deg,#6c63ff,#3f3d9c);
        color:white; padding:2rem; border-radius:12px; text-align:center; margin-bottom:2rem; }
    .card { background:white; padding:1.5rem; border-radius:12px;
        box-shadow:0 2px 12px rgba(108,99,255,0.12); margin-bottom:1rem; }
    .badge-admin    { background:#ff4757; color:white; padding:2px 8px; border-radius:12px; font-size:0.75rem; }
    .badge-faculty  { background:#2ed573; color:white; padding:2px 8px; border-radius:12px; font-size:0.75rem; }
    .badge-student  { background:#6c63ff; color:white; padding:2px 8px; border-radius:12px; font-size:0.75rem; }
    .badge-staff    { background:#ffa502; color:white; padding:2px 8px; border-radius:12px; font-size:0.75rem; }
    .stButton>button { background:linear-gradient(135deg,#6c63ff,#3f3d9c);
        color:white; border:none; border-radius:8px; padding:0.5rem 1.5rem; }
    .stButton>button:hover { transform:translateY(-1px); box-shadow:0 4px 12px rgba(108,99,255,0.4); }
</style>
""", unsafe_allow_html=True)

# ── Sidebar navigation ────────────────────────────────────────
with st.sidebar:
    st.markdown("## 🎓 Smart Campus EMS")
    st.divider()

    if st.session_state.token:
        user = st.session_state.user
        role = user.get("role","STUDENT")
        st.markdown(f"👤 **{user.get('name','User')}**")
        badge = {"ADMIN":"badge-admin","FACULTY":"badge-faculty",
                 "STAFF":"badge-staff"}.get(role,"badge-student")
        st.markdown(f'<span class="{badge}">{role}</span>', unsafe_allow_html=True)
        st.divider()

        pages = ["🏠 Dashboard", "📅 Events", "🗓️ My Events", "📍 Venues", "📢 Announcements"]
        if role == "ADMIN":
            pages.append("⚙️ Admin Panel")
        page = st.radio("Navigate", pages, label_visibility="collapsed")

        st.divider()
        if st.button("🚪 Logout", use_container_width=True):
            st.session_state.token = None
            st.session_state.user = None
            st.rerun()
    else:
        page = st.radio("Navigate", ["🔐 Login", "📝 Register", "🔑 Forgot Password"],
                        label_visibility="collapsed")

# ── Page routing ──────────────────────────────────────────────
if st.session_state.token:
    if "Dashboard" in page:
        from streamlit_app.pages import dashboard; dashboard.show()
    elif "Events" in page and "My" not in page:
        from streamlit_app.pages import events; events.show()
    elif "My Events" in page:
        from streamlit_app.pages import my_events; my_events.show()
    elif "Venues" in page:
        from streamlit_app.pages import venues; venues.show()
    elif "Announcements" in page:
        from streamlit_app.pages import announcements; announcements.show()
    elif "Admin" in page:
        from streamlit_app.pages import admin; admin.show()
else:
    if "Login" in page:
        from streamlit_app.pages import login; login.show()
    elif "Register" in page:
        from streamlit_app.pages import register; register.show()
    elif "Forgot" in page:
        from streamlit_app.pages import forgot_password; forgot_password.show()
