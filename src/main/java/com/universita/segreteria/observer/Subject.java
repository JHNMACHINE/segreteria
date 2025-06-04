package com.universita.segreteria.observer;

import com.universita.segreteria.model.Voto;

public interface Subject {
    void attach(Observer o);

    void detach(Observer o);

    void notifyObservers(Voto voto);
}
