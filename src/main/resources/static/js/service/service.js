// service/service.js

export function eseguiOperazione(nomeOperazione, parametri = []) {
  return fetch('/api/v1/operazione', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    body: JSON.stringify({ nomeOperazione, parametri })
  }).then(res => {
    if (!res.ok) throw new Error("Errore nella richiesta");

    // Controlla se la risposta è vuota
    const contentLength = res.headers.get('Content-Length');
    if (contentLength === '0') {
      return null; // Ritorna null per risposte vuote
    }

    // Prova a parsare solo se c'è contenuto
    return res.json();
  });
}



