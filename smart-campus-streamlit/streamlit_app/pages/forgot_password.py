import streamlit as st
import sys, os
sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))
from api_utils import post

def show():
    st.markdown('<div class="main-header"><h1>🔑 Reset Password</h1></div>', unsafe_allow_html=True)
    col1, col2, col3 = st.columns([1, 2, 1])
    with col2:
        step = st.session_state.get("fp_step", 1)

        if step == 1:
            with st.form("fp_email"):
                email = st.text_input("📧 Your Email")
                go    = st.form_submit_button("Send OTP", use_container_width=True)
            if go and email:
                post("/auth/forgot-password", json={"email": email})
                st.session_state.fp_email = email
                st.session_state.fp_step  = 2
                st.success("If the email exists, an OTP was sent.")
                st.rerun()

        elif step == 2:
            with st.form("fp_reset"):
                otp      = st.text_input("🔢 OTP")
                new_pass = st.text_input("🔒 New Password", type="password")
                go       = st.form_submit_button("Reset Password", use_container_width=True)
            if go:
                data, err = post("/auth/reset-password",
                                 json={"email": st.session_state.fp_email,
                                       "otp": otp, "newPassword": new_pass})
                if err:
                    st.error(err)
                else:
                    st.success("✅ Password reset! Please login.")
                    st.session_state.fp_step = 1
