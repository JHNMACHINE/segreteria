// service/segretario.js
import { eseguiOperazione } from '/js/service/service.js';

export function getProfilo() {
    return eseguiOperazione('getProfilo');
}

export function inserisciStudente(studenteDTO) {
    return eseguiOperazione('inserisciStudente', [studenteDTO]);  // Inserisci un nuovo studente
}

export function inserisciDocente(docenteDTO) {
    return eseguiOperazione('inserisciDocente', [docenteDTO]);  // Inserisci un nuovo docente
}

export function confermaVoto(studenteDTO, votoId) {
    return eseguiOperazione('confermaVoto', [studenteDTO, votoId]);  // Conferma un voto per uno studente
}

export function cambiaPianoDiStudi(studenteId, nuovoPiano) {
  // Verifica che studenteId sia un numero (Long)
  const studenteIdNumerico = Number(studenteId);
  if (isNaN(studenteIdNumerico)) {
    throw new Error("ID dello studente non valido");
  }
  return eseguiOperazione('cambiaPianoDiStudi', [studenteIdNumerico, nuovoPiano]);
}

export function cercaStudente(nome, cognome) {
    return eseguiOperazione('cercaStudente', [nome, cognome]);  // Cerca uno studente per nome e cognome
}

export function cercaStudentePerMatricola(matricola) {
    return eseguiOperazione('cercaStudentePerMatricola', [matricola]);  // Cerca uno studente per matricola
}

export function getAllStudenti() {
  return eseguiOperazione("getAllStudenti");
}

export function getAllDocenti() {
  return eseguiOperazione("getAllDocenti");
}

export async function creaDocente({ nome, cognome, email, pianoDiStudi }) {
    const dto = {
        nome,
        cognome,
        email,
        pianoDiStudi
    };

    return await eseguiOperazione('creaDocente', [dto]);
}
