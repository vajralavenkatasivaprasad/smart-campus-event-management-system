import streamlit as st
import sys, os
sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))
from api_utils import get, post, delete

def show():
    st.markdown('<div class="main-header"><h1>📢 Announcements</h1><p>Campus news and updates</p></div>',
                unsafe_allow_html=True)

    data, err = get("/announcements")
    if err:
        st.error(err)
        return

    if not data:
        st.info("No announcements.")
        return

    for ann in data:
        with st.container(border=True):
            pin = "📌 " if ann.get("isPinned") else ""
            role_badge = ann.get("targetRole","ALL")
            st.markdown(f"### {pin}{ann['title']}  `{role_badge}`")
            st.write(ann["content"])
            author = ann.get("author", {})
            st.caption(f"By: {author.get('name','N/A')} · {ann.get('createdAt','')[:10]}")

            user_role = st.session_state.user.get("role","STUDENT")
            if user_role == "ADMIN":
                if st.button("🗑️ Delete", key=f"del_ann_{ann['id']}"):
                    delete(f"/announcements/{ann['id']}")
                    st.rerun()

    # Create announcement
    user_role = st.session_state.user.get("role","STUDENT")
    if user_role in ("ADMIN","FACULTY"):
        st.divider()
        with st.expander("➕ Post Announcement"):
            with st.form("post_ann"):
                title   = st.text_input("Title *")
                content = st.text_area("Content *")
                target  = st.selectbox("Target Role", ["ALL","STUDENT","FACULTY","STAFF"])
                pinned  = st.checkbox("Pin this announcement")
                sub     = st.form_submit_button("📢 Post", use_container_width=True)

            if sub:
                if not title or not content:
                    st.error("Title and content are required.")
                else:
                    _, err2 = post("/announcements",
                                   json={"title":title,"content":content,
                                         "targetRole":target,"isPinned":pinned})
                    if err2: st.error(err2)
                    else:    st.success("✅ Announcement posted!"); st.rerun()
