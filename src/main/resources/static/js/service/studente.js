// service/studente.js
import { eseguiOperazione } from '/js/service/service.js';

// Non serve più getEmailOrRedirect o getEmailFromToken.
// Se il token è mancante/fuori validità, eseguiOperazione fallirà o il backend restituirà 401/errore e potrai fare logout nel catch.

export function getInfoStudente() {
    // Backend estrae lo studente dal SecurityContext
    return eseguiOperazione('getInfoStudente',[]);
}

export function getPianoDiStudi() {
    return eseguiOperazione('getPianoDiStudi',[]);
}

export function getCarriera() {

    return eseguiOperazione('getCarriera', []);

}

export function getEsamiPrenotabili() {
    return eseguiOperazione('esamiPrenotabili', []);
}

export function getVotiDaAccettare() {
    return eseguiOperazione('getVotiDaAccettare', []);
}

export function aggiornaStatoVoto(votoId, accettato) {
    if (typeof votoId !== 'number') {
        return Promise.reject("ID voto deve essere un numero");
    }

    return eseguiOperazione('aggiornaStatoVoto', [votoId, accettato])
        .then((result) => {
            // Gestisce sia risposte JSON che vuote
            if (accettato) {
                alert("Richiesta inviata alla segreteria. Attendi conferma definitiva.");
            }
            return true;
        })
        .catch(err => {
            console.error("Errore aggiornamento stato voto:", err);
            throw err;
        });
}

export function confermaVoto(votoId) {
    return eseguiOperazione('confermaVoto', [votoId]);
}

export function getVotiInAttesa() {
    return eseguiOperazione('getVotiInAttesa', []);
}

export function prenotaEsame(appelloId) {
    if (!appelloId) {
        return Promise.reject(new Error("ID appello non valido"));
    }
    return eseguiOperazione('prenotaEsame', [appelloId]);
}

export function pagaTassa(nomeTassa) {
    return eseguiOperazione('pagaTassa', [nomeTassa])
        .catch(err => {
            if (err instanceof SyntaxError) {
                console.warn("");
                return;
            }
            throw err;
        });
}