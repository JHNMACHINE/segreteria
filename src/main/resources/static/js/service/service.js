// service/service.js

export function getToken() {
  return localStorage.getItem('token');
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
  const token = getToken();
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