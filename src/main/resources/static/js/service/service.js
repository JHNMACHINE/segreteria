// service/service.js

import { refreshToken, parseJwt } from '/js/auth.js';

const REFRESH_THRESHOLD_MS = 60000;  // 1 minuto prima della scadenza

export async function getToken() {
  let token = localStorage.getItem("token");
  if (!token) return null;

  const decoded = parseJwt(token);
  if (!decoded || !decoded.exp) return null;

  const nowMs = Date.now();
  const expMs = decoded.exp * 1000;  // converti exp da secondi a ms

  if (expMs - nowMs < REFRESH_THRESHOLD_MS) {
    token = await refreshToken();
  }

  return token;
}



export function getEmailFromToken() {
  const token = getToken();
  if (!token) return null;

  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.sub || null;
  } catch (e) {
    console.error('Token non valido:', e);
    return null;
  }
}

export function eseguiOperazione(nomeOperazione, parametri = []) {
  const token = getEmailFromToken();
  if (!token) throw new Error("Token non trovato");

  return fetch('/api/v1/operazione', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token
    },
    body: JSON.stringify({ nomeOperazione, parametri })
  }).then(res => {
    if (!res.ok) throw new Error("Errore nella richiesta");
    return res.json();
  });
}

export function logout() {
  localStorage.removeItem('token');
  window.location.href = "/";
}
