"""
Shared API helper used by all Streamlit pages.
"""
import requests
import streamlit as st


def get_headers():
    token = st.session_state.get("token")
    return {"Authorization": f"Bearer {token}", "Content-Type": "application/json"} if token else {}


def api(method: str, path: str, **kwargs):
    """Wrapper around requests that returns (data, error_message)."""
    base = st.session_state.get("api_base", "http://localhost:8080/api")
    url = f"{base}{path}"
    try:
        resp = requests.request(method, url, headers=get_headers(), timeout=15, **kwargs)
        if resp.ok:
            return resp.json(), None
        try:
            msg = resp.json().get("message", resp.text)
        except Exception:
            msg = resp.text
        return None, msg
    except requests.exceptions.ConnectionError:
        return None, "❌ Cannot connect to backend. Is the Spring Boot server running?"
    except Exception as e:
        return None, str(e)


def get(path, **kwargs):   return api("GET",    path, **kwargs)
def post(path, **kwargs):  return api("POST",   path, **kwargs)
def put(path, **kwargs):   return api("PUT",    path, **kwargs)
def delete(path, **kwargs): return api("DELETE", path, **kwargs)
