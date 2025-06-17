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

export function getInfoStudente() {
    const email = getEmailOrRedirect();
    return eseguiOperazione('getInfoStudente', [email]);
}

export function getPianoDiStudi() {
    const email = getEmailOrRedirect();
    return eseguiOperazione('getPianoDiStudi', [email]);
}

export function getCarriera(){
    const email = getEmailOrRedirect();
    return eseguiOperazione('getCarriera', [email]);
}

export function getEsamiPrenotabili() {
    const email = getEmailOrRedirect();
    return eseguiOperazione('esamiPrenotabili', [email]);
}

export function getVotiDaAccettare() {
    const email = getEmailOrRedirect();
    return eseguiOperazione('getVotiDaAccettare', [email]);
}

export function aggiornaStatoVoto(votoId, accettato) {
    const email = getEmailOrRedirect();
    return eseguiOperazione('aggiornaStatoVoto', [email, votoId, accettato]);
}

export function prenotaEsame(appelloId) {
    if (!appelloId) {
        console.error("❌ ID appello non valido:", appelloId);
        throw new Error("ID appello non valido");
    }
    const email = getEmailOrRedirect();
    return eseguiOperazione('prenotaEsame', [email, appelloId]);
}

