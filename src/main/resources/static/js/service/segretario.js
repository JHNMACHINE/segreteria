// service/segretario.js
import { getEmailFromToken, eseguiOperazione } from '/js/service/service.js';

function getEmailOrRedirect() {
    const email = getEmailFromToken();  // Usa la funzione getEmailFromToken
    if (!email) {
        alert("Sessione scaduta. Effettua di nuovo il login.");
        window.location.href = "/";  // Redirige alla pagina di login se la sessione è scaduta
    }
    return email;
}

export function getProfilo() {
    const email = getEmailOrRedirect();
    return eseguiOperazione('getProfilo', [email]);
}

export function inserisciStudente(studenteDTO) {
    const email = getEmailOrRedirect();  // Ottieni l'email tramite getEmailFromToken
    return eseguiOperazione('inserisciStudente', [email, studenteDTO]);  // Inserisci un nuovo studente
}

export function inserisciDocente(docenteDTO) {
    const email = getEmailOrRedirect();  // Ottieni l'email tramite getEmailFromToken
    return eseguiOperazione('inserisciDocente', [email, docenteDTO]);  // Inserisci un nuovo docente
}

export function confermaVoto(studenteDTO, votoId) {
    const email = getEmailOrRedirect();  // Ottieni l'email tramite getEmailFromToken
    return eseguiOperazione('confermaVoto', [email, studenteDTO, votoId]);  // Conferma un voto per uno studente
}

export function cambiaPianoDiStudi(studenteId, nuovoPiano) {
  const email = getEmailOrRedirect();  // Ottieni l'email tramite getEmailFromToken
  // Verifica che studenteId sia un numero (Long)
  const studenteIdNumerico = Number(studenteId);
  if (isNaN(studenteIdNumerico)) {
    throw new Error("ID dello studente non valido");
  }
  return eseguiOperazione('cambiaPianoDiStudi', [email, studenteIdNumerico, nuovoPiano]);
}



export function cercaStudente(nome, cognome) {
    const email = getEmailOrRedirect();  // Ottieni l'email tramite getEmailFromToken
    return eseguiOperazione('cercaStudente', [email, nome, cognome]);  // Cerca uno studente per nome e cognome
}

export function cercaStudentePerMatricola(matricola) {
    const email = getEmailOrRedirect();  // Ottieni l'email tramite getEmailFromToken
    return eseguiOperazione('cercaStudentePerMatricola', [email, matricola]);  // Cerca uno studente per matricola
}

export function getAllStudenti() {
  const email = getEmailFromToken();
  return eseguiOperazione("getAllStudenti", [email]);
}

export function getAllDocenti() {
  const email = getEmailFromToken();
  return eseguiOperazione("getAllDocenti", [email]);
}
