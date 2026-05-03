import streamlit as st
import sys, os
sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))
from api_utils import get

def show():
    st.markdown('<div class="main-header"><h1>🗓️ My Events</h1><p>Your registrations and QR tickets</p></div>',
                unsafe_allow_html=True)

    data, err = get("/events/my-events")
    if err:
        st.error(err)
        return

    if not data:
        st.info("You haven't registered for any events yet.")
        return

    st.success(f"You are registered for **{len(data)}** event(s).")

    for reg in data:
        evt = reg.get("event", {})
        with st.container(border=True):
            col1, col2 = st.columns([3, 1])
            with col1:
                st.markdown(f"### 🎫 {evt.get('title','N/A')}")
                st.caption(f"📂 {evt.get('category','N/A')}  |  📊 {evt.get('status','N/A')}")
                if evt.get("locationName"):
                    st.caption(f"📍 {evt['locationName']}")
                st.markdown(f"**Ticket #:** `{reg.get('ticketNumber','N/A')}`")
                status = reg.get("registrationStatus","CONFIRMED")
                color  = "🟢" if status == "CONFIRMED" else "🔴"
                st.markdown(f"**Status:** {color} {status}")
                payment = reg.get("paymentStatus","PAID")
                st.markdown(f"**Payment:** {'✅' if payment=='PAID' else '⏳'} {payment}")

            with col2:
                qr = reg.get("qrCode","")
                if qr and qr.startswith("data:image"):
                    # Strip the data URL prefix for st.image
                    import base64
                    img_data = qr.split(",", 1)[1]
                    img_bytes = base64.b64decode(img_data)
                    st.image(img_bytes, caption="QR Ticket", width=150)
                else:
                    st.info("QR not available")
