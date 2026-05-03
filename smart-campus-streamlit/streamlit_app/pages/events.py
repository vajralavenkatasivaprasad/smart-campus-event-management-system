import streamlit as st
import sys, os
sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))
from api_utils import get, post, delete

CATEGORIES = ["ALL", "ACADEMIC", "CULTURAL", "SPORTS", "WORKSHOP", "SEMINAR", "CONFERENCE", "SOCIAL", "OTHER"]

def show():
    st.markdown('<div class="main-header"><h1>📅 Events</h1><p>Browse and register for campus events</p></div>',
                unsafe_allow_html=True)

    # Filters
    col1, col2, col3 = st.columns([3, 2, 1])
    search   = col1.text_input("🔍 Search events", placeholder="Type event name...")
    category = col2.selectbox("📂 Category", CATEGORIES)
    page_num = col3.number_input("Page", min_value=0, value=0, step=1)

    params = f"?page={page_num}&size=9"
    if search:       params += f"&search={search}"
    if category != "ALL": params += f"&category={category}"

    data, err = get(f"/events{params}")
    if err:
        st.error(err)
        return

    events       = data.get("events", [])
    total_pages  = data.get("totalPages", 1)
    total_items  = data.get("totalElements", 0)

    st.caption(f"Showing {len(events)} of {total_items} events  |  Page {page_num+1}/{max(total_pages,1)}")

    if not events:
        st.info("No events found.")
        return

    # Event cards in 3-column grid
    for i in range(0, len(events), 3):
        cols = st.columns(3)
        for j, col in enumerate(cols):
            if i + j >= len(events): break
            evt = events[i + j]
            with col:
                with st.container(border=True):
                    st.markdown(f"### {evt['title']}")
                    st.caption(f"📂 {evt.get('category','OTHER')}  |  📊 {evt.get('status','PUBLISHED')}")
                    desc = evt.get("description","")
                    st.write(desc[:120] + ("..." if len(desc) > 120 else ""))
                    attendees = evt.get("currentAttendees", 0)
                    max_att   = evt.get("maxAttendees")
                    capacity  = f"{attendees}/{max_att}" if max_att else str(attendees)
                    st.caption(f"👥 {capacity} registered  |  💰 {'Free' if evt.get('isFree') else 'Paid'}")
                    if evt.get("locationName"):
                        st.caption(f"📍 {evt['locationName']}")

                    col_r, col_u = st.columns(2)
                    evt_id = evt["id"]

                    if col_r.button("📋 Register", key=f"reg_{evt_id}", use_container_width=True):
                        resp, err2 = post(f"/events/{evt_id}/register")
                        if err2:
                            st.error(err2)
                        else:
                            ticket = resp.get("ticketNumber", "N/A")
                            st.success(f"✅ Registered! Ticket: `{ticket}`")

                    if col_u.button("❌ Cancel",  key=f"unreg_{evt_id}", use_container_width=True):
                        resp, err2 = delete(f"/events/{evt_id}/unregister")
                        if err2:
                            st.error(err2)
                        else:
                            st.info("Unregistered.")

    # Create event (ADMIN/FACULTY/STAFF)
    role = st.session_state.user.get("role","STUDENT")
    if role in ("ADMIN","FACULTY","STAFF"):
        st.divider()
        with st.expander("➕ Create New Event"):
            with st.form("create_event"):
                c1, c2 = st.columns(2)
                title    = c1.text_input("Title *")
                cat      = c2.selectbox("Category", CATEGORIES[1:])
                desc     = st.text_area("Description")
                c3, c4   = st.columns(2)
                start    = c3.text_input("Start Date (YYYY-MM-DDTHH:MM:SS)", placeholder="2025-09-01T09:00:00")
                end      = c4.text_input("End Date   (YYYY-MM-DDTHH:MM:SS)", placeholder="2025-09-01T17:00:00")
                c5, c6   = st.columns(2)
                max_att  = c5.number_input("Max Attendees", min_value=1, value=100)
                loc_name = c6.text_input("Location Name")
                is_free  = st.checkbox("Free Event", value=True)
                submitted = st.form_submit_button("🚀 Create Event", use_container_width=True)

            if submitted:
                if not title or not start or not end:
                    st.error("Title, start date, and end date are required.")
                else:
                    resp, err2 = post("/events", json={
                        "title": title, "description": desc, "category": cat,
                        "startDate": start, "endDate": end, "maxAttendees": max_att,
                        "locationName": loc_name, "isFree": is_free
                    })
                    if err2: st.error(err2)
                    else:    st.success("✅ Event created!"); st.rerun()
