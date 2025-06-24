// service/docente.js

import { eseguiOperazione } from '/js/service/service.js';

export function getInfoDocente() {
    return eseguiOperazione('getInfoDocente', []);
}

export function getAppelli(){
    return eseguiOperazione('getAppelli', []);
}


export function inserisciVoto(id,data){
    return eseguiOperazione('inserisciVoto', [id,data]);
}

export function creaAppello(nome,data,aula){
    return eseguiOperazione('creaEsame',[nome,data,aula]);
}

export function getAuleDisponibili(data){
    return eseguiOperazione('getAuleDisponibili',[data]);
}

export function eliminaAppello(id){
    return eseguiOperazione('eliminaEsame',[id]);
}

export function trovaStudentiPerAppello(appelloId) {
    const id = Number(appelloId);
    return eseguiOperazione('trovaStudentiPerAppello', [id]);
}