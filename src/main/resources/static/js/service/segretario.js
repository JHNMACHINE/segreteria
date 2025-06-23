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

export function confermaVoto(votoId) {
    // votoId potrebbe essere una stringa, forziamo il cast a numero:
    const idNumerico = Number(votoId);
    if (isNaN(idNumerico)) {
      throw new Error("ID voto non valido");
    }
    return eseguiOperazione('confermaVoto', [ idNumerico ])
      .catch(err => {
        console.error("Errore conferma voto:", err);
        throw new Error("Operazione non riuscita: " + (err.message || "Errore sconosciuto"));
      });
}


export function getVotiInAttesa() {
    return eseguiOperazione('getVotiInAttesa', []);
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

export async function creaDocente({ nome, cognome, email, pianoDiStudi,corso }) {
    const dto = {
        nome,
        cognome,
        email,
        pianoDiStudi,
        corso
    };

    return await eseguiOperazione('creaDocente', [dto]);
}

export async function creaStudente({ nome, cognome, email, pianoDiStudi }) {
    const dto = {
        nome,
        cognome,
        email,
        pianoDiStudi
    };
    console.log(dto)
    return await eseguiOperazione('creaStudente', [dto]);
}

export  function getEsamiDisponibiliPerPiano(piano){
    return eseguiOperazione("getEsamiDisponibiliPerPiano",[piano]);

}