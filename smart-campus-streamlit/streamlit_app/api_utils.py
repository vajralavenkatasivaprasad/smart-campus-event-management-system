"""
Shared API helper used by all Streamlit pages.
"""
import requests
import streamlit as st

RAILWAY_BASE = "https://smart-campus-event-management-system-production.up.railway.app/api"

def get_headers():
    token = st.session_state.get("token")
    return {"Authorization": f"Bearer {token}", "Content-Type": "application/json"} if token else {"Content-Type": "application/json"}

def api(method: str, path: str, **kwargs):
    """Wrapper around requests that returns (data, error_message)."""
    base = st.session_state.get("api_base", RAILWAY_BASE)
    url = f"{base}{path}"
    try:
        resp = requests.request(method, url, headers=get_headers(), timeout=15, **kwargs)
        if resp.ok:
            try:
                return resp.json(), None
            except Exception:
                return resp.text, None
        try:
            msg = resp.json().get("message", resp.text)
        except Exception:
            msg = resp.text
        return None, f"Error {resp.status_code}: {msg}"
    except requests.exceptions.ConnectionError:
        return None, f"❌ Cannot connect to backend at {base}"
    except requests.exceptions.Timeout:
        return None, "❌ Request timed out. Backend may be sleeping, try again."
    except Exception as e:
        return None, str(e)

def get(path, **kwargs):    return api("GET",    path, **kwargs)
def post(path, **kwargs):   return api("POST",   path, **kwargs)
def put(path, **kwargs):    return api("PUT",    path, **kwargs)
def delete(path, **kwargs): return api("DELETE", path, **kwargs)
