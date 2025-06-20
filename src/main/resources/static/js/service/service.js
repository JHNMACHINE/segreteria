// service/service.js

export function eseguiOperazione(nomeOperazione, parametri = []) {
  return fetch('/api/v1/operazione', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',  // <-- manda i cookie con la richiesta
    body: JSON.stringify({ nomeOperazione, parametri })
  }).then(res => {
    if (!res.ok) throw new Error("Errore nella richiesta");
    return res.json();
  });
}



