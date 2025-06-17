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

export function getAppelli(){
    const email = getEmailOrRedirect();
    return eseguiOperazione('getAppelli', [email]);
}


export function inserisciVoto(data){
    const email = getEmailOrRedirect();
    return eseguiOperazione('inserisciVoto', [email, data]);
}

export function creaAppello(data){
    const email = getEmailOrRedirect();
    return eseguiOperazione('creaAppello', [email, data]);
}