// service/service.js
export function eseguiOperazione(nomeOperazione, parametri = []) {
  return fetch('/api/v1/operazione', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ nomeOperazione, parametri })
  }).then(async res => {
    const text = await res.text();
    if (!res.ok) {
      let msg = text;
      try {
        const obj = JSON.parse(text);
        if (obj.message) msg = obj.message;
      } catch {}
      throw new Error(msg || `Errore ${res.status}`);
    }
    if (!text) return null;
    try {
      return JSON.parse(text);
    } catch {
      return null;
    }
  });
}


