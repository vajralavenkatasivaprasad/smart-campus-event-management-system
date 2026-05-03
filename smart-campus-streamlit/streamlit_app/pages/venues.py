import streamlit as st
import sys, os
sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))
from api_utils import get

def show():
    st.markdown('<div class="main-header"><h1>📍 Campus Venues</h1><p>Explore event locations</p></div>',
                unsafe_allow_html=True)

    data, err = get("/venues")
    if err:
        st.error(err)
        return

    if not data:
        st.info("No venues found.")
        return

    # Collect coords for map
    map_data = []
    for v in data:
        if v.get("latitude") and v.get("longitude"):
            map_data.append({"lat": v["latitude"], "lon": v["longitude"], "name": v["name"]})

    if map_data:
        import pandas as pd
        df = pd.DataFrame(map_data)
        st.map(df, zoom=15)
        st.divider()

    cols = st.columns(3)
    for i, v in enumerate(data):
        with cols[i % 3]:
            with st.container(border=True):
                avail = "🟢 Available" if v.get("isAvailable", True) else "🔴 Unavailable"
                st.markdown(f"### 🏛️ {v['name']}")
                st.caption(avail)
                st.write(v.get("description",""))
                st.metric("Capacity", v.get("capacity", "N/A"))
                if v.get("location"):
                    st.caption(f"📍 {v['location']}")
                if v.get("amenities"):
                    st.caption(f"✨ {v['amenities']}")
