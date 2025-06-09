package com.universita.segreteria.notifier;

import com.universita.segreteria.model.Voto;
import com.universita.segreteria.observer.Observer;
import com.universita.segreteria.observer.Subject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AcceptationNotifier implements Subject {

    private final List<Observer> observers = new ArrayList<>();

    @Override
    public void attach(Observer o) {
        observers.add(o);
    }

    @Override
    public void detach(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(Voto voto) {
        for (Observer o : observers) {
            o.update(voto);
        }
    }
}
