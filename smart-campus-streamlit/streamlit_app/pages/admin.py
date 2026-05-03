import streamlit as st
import sys, os
sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))
from api_utils import get, delete, put

def show():
    user = st.session_state.user
    if user.get("role") != "ADMIN":
        st.error("⛔ Access denied. Admins only.")
        return

    st.markdown('<div class="main-header"><h1>⚙️ Admin Panel</h1><p>System administration</p></div>',
                unsafe_allow_html=True)

    # Stats
    stats, _ = get("/admin/dashboard-stats")
    if stats:
        c1, c2, c3, c4, c5, c6 = st.columns(6)
        c1.metric("👥 Users",         stats.get("totalUsers",0))
        c2.metric("📅 Events",        stats.get("totalEvents",0))
        c3.metric("🟢 Published",     stats.get("publishedEvents",0))
        c4.metric("✅ Completed",     stats.get("completedEvents",0))
        c5.metric("🎟️ Registrations", stats.get("totalRegistrations",0))
        c6.metric("📍 Venues",        stats.get("totalVenues",0))

    st.divider()
    tab1, tab2, tab3 = st.tabs(["👥 Users", "📅 Events", "📊 Registrations"])

    # ── Users tab ──────────────────────────────────────────────
    with tab1:
        st.subheader("All Users")
        users, err = get("/admin/users")
        if err:
            st.error(err)
        elif users:
            import pandas as pd
            df = pd.DataFrame([{
                "ID": u["id"], "Name": u["name"], "Email": u["email"],
                "Role": u["role"], "Verified": u.get("isVerified", False)
            } for u in users])
            st.dataframe(df, use_container_width=True)

            st.subheader("Delete User")
            del_id = st.number_input("User ID to delete", min_value=1, step=1)
            if st.button("🗑️ Delete User", type="primary"):
                _, err2 = delete(f"/admin/users/{del_id}")
                if err2: st.error(err2)
                else:    st.success(f"User {del_id} deleted."); st.rerun()

    # ── Events tab ─────────────────────────────────────────────
    with tab2:
        st.subheader("Manage Event Status")
        events_data, _ = get("/events?size=100")
        if events_data:
            events = events_data.get("events", [])
            for evt in events:
                with st.container(border=True):
                    c1, c2, c3 = st.columns([3, 2, 1])
                    c1.write(f"**{evt['title']}**  (`{evt.get('status','N/A')}`)")
                    new_status = c2.selectbox("New Status",
                                              ["DRAFT","PUBLISHED","CANCELLED","COMPLETED"],
                                              key=f"status_{evt['id']}",
                                              index=["DRAFT","PUBLISHED","CANCELLED","COMPLETED"].index(
                                                  evt.get("status","DRAFT")))
                    if c3.button("Update", key=f"upd_{evt['id']}"):
                        _, err2 = put(f"/admin/events/{evt['id']}/status",
                                      json={"status": new_status})
                        if err2: st.error(err2)
                        else:    st.success("Updated!"); st.rerun()

    # ── Registrations tab ──────────────────────────────────────
    with tab3:
        st.subheader("Event Registrations")
        evt_id = st.number_input("Enter Event ID to view registrations", min_value=1, step=1)
        if st.button("📋 Fetch Registrations"):
            regs, err2 = get(f"/events/{evt_id}/registrations")
            if err2:
                st.error(err2)
            elif regs:
                import pandas as pd
                df = pd.DataFrame([{
                    "Ticket": r.get("ticketNumber",""),
                    "User":   r.get("user",{}).get("name",""),
                    "Email":  r.get("user",{}).get("email",""),
                    "Status": r.get("registrationStatus",""),
                } for r in regs])
                st.dataframe(df, use_container_width=True)
            else:
                st.info("No registrations found.")
