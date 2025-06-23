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

export function creaAppello(nome,data){
    return eseguiOperazione('creaEsame',[nome,data]);
}

export function trovaStudentiPerEsame(corso){
    return eseguiOperazione('trovaStudentiPerEsame', [corso]);
}