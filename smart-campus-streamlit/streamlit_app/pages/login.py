import streamlit as st
import sys, os
sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))
from api_utils import post

def show():
    st.markdown('<div class="main-header"><h1>🔐 Login</h1><p>Smart Campus Event Management System</p></div>',
                unsafe_allow_html=True)

    col1, col2, col3 = st.columns([1, 2, 1])
    with col2:
        with st.form("login_form"):
            email    = st.text_input("📧 Email",    placeholder="your@campus.edu")
            password = st.text_input("🔒 Password", type="password")
            submitted = st.form_submit_button("Login", use_container_width=True)

        if submitted:
            if not email or not password:
                st.error("Please fill in all fields.")
            else:
                data, err = post("/auth/login", json={"email": email, "password": password})
                if err:
                    st.error(err)
                else:
                    st.session_state.token = data["token"]
                    st.session_state.user  = data["user"]
                    st.success("✅ Login successful!")
                    st.rerun()

        st.markdown("---")
        st.info("**Demo Credentials**\n\n"
                "Admin: `admin@campus.edu` / `password123`\n\n"
                "Student: `student@campus.edu` / `password123`")
