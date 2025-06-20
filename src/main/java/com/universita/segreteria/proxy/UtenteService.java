package com.universita.segreteria.proxy;

public interface UtenteService {
    //Creo un object di esegui operazione passando in input una stringa ed un oggetto o una lista di oggetti
    //Object... è var args, permette il passaggio di uno o più object di diverso tipo
    Object eseguiOperazione(String operazione,String subject, Object... parametri);
}
