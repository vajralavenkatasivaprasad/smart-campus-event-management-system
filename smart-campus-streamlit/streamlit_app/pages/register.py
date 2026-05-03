import streamlit as st
import sys, os
sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))
from api_utils import post

def show():
    st.markdown('<div class="main-header"><h1>📝 Register</h1><p>Create your campus account</p></div>',
                unsafe_allow_html=True)

    col1, col2, col3 = st.columns([1, 2, 1])
    with col2:
        if "otp_step" not in st.session_state:
            st.session_state.otp_step = False
            st.session_state.reg_email = ""

        if not st.session_state.otp_step:
            with st.form("register_form"):
                name     = st.text_input("👤 Full Name")
                email    = st.text_input("📧 Email")
                password = st.text_input("🔒 Password", type="password")
                phone    = st.text_input("📱 Phone (optional)")
                role     = st.selectbox("🎭 Role", ["STUDENT", "FACULTY", "STAFF"])
                submitted = st.form_submit_button("Register", use_container_width=True)

            if submitted:
                if not name or not email or not password:
                    st.error("Name, email and password are required.")
                else:
                    data, err = post("/auth/register",
                                     json={"name": name, "email": email,
                                           "password": password, "phone": phone, "role": role})
                    if err:
                        st.error(err)
                    else:
                        st.success("✅ Registered! Check your email for the OTP.")
                        st.session_state.otp_step = True
                        st.session_state.reg_email = email
                        st.rerun()
        else:
            st.info(f"📧 OTP sent to **{st.session_state.reg_email}**")
            with st.form("otp_form"):
                otp = st.text_input("🔢 Enter 6-digit OTP", max_chars=6)
                col_v, col_r = st.columns(2)
                verify  = col_v.form_submit_button("✅ Verify", use_container_width=True)
                resend  = col_r.form_submit_button("🔄 Resend", use_container_width=True)

            if verify and otp:
                data, err = post("/auth/verify-otp",
                                 json={"email": st.session_state.reg_email, "otp": otp})
                if err:
                    st.error(err)
                else:
                    st.session_state.token = data["token"]
                    st.session_state.user  = data["user"]
                    st.session_state.otp_step = False
                    st.success("🎉 Email verified! Logging you in...")
                    st.rerun()

            if resend:
                post("/auth/resend-otp", json={"email": st.session_state.reg_email})
                st.info("OTP resent!")
