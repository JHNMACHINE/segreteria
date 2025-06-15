// service/studente.js

import { eseguiOperazione, getEmailFromToken } from '/js/service/service.js';

function getEmailOrRedirect() {
    const email = getEmailFromToken(); // Nessun argomento necessario
    if (!email) {
        alert("Sessione scaduta. Effettua di nuovo il login.");
        window.location.href = "/";
    }
    return email;
}

export function getInfoDocente() {
    const email = getEmailOrRedirect();
    return eseguiOperazione('getInfoDocente', [email]);
}