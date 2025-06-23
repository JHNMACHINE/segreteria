// service/docente.js

import { eseguiOperazione } from '/js/service/service.js';

export function getInfoDocente() {
    return eseguiOperazione('getInfoDocente', []);
}

export function getAppelli(){
    return eseguiOperazione('getAppelli', []);
}


export function inserisciVoto(data){
    return eseguiOperazione('inserisciVoto', [data]);
}

export function creaAppello(data){
    return eseguiOperazione('creaAppello',  [data]);
}

export function trovaStudentiPerEsame(corso){
    return eseguiOperazione('trovaStudentiPerEsame', [corso]);
}