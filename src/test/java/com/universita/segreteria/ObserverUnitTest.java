package com.universita.segreteria;

import com.universita.segreteria.model.*;
import com.universita.segreteria.observer.SegreteriaObserver;
import com.universita.segreteria.observer.StudenteObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class ObserverUnitTest {

    private Studente studente;
    private Esame esame;
    private Voto voto;

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        // Redirect console output per testare System.out
        System.setOut(new PrintStream(output));

        studente = new Studente();
        studente.setId(1L);
        studente.setNome("Mario Rossi");

        esame = new Esame();
        esame.setNome("Analisi 1");
        esame.setStatoEsame(StatoEsame.AMMESSO);

        voto = new Voto();
        voto.setStudente(studente);
        voto.setEsame(esame);
        voto.setVoto(28);
        voto.setStato(StatoVoto.ATTESA);
    }

    @Test
    void testStudenteObserver_conStudentePresente() {
        StudenteObserver observer = new StudenteObserver(studente);
        observer.update(voto);

        String expected = "🔔 Notifica per Mario Rossi: è stato inserito il voto 28 per l’esame Analisi 1";
        assertTrue(output.toString().contains(expected));
    }

    @Test
    void testStudenteObserver_conStudenteAssente() {
        esame.setStatoEsame(StatoEsame.ASSENTE);

        StudenteObserver observer = new StudenteObserver(studente);
        observer.update(voto);

        String expected = "Lo studente risulta assente";
        assertTrue(output.toString().contains(expected));
    }

    @Test
    void testSegreteriaObserver_conVotoAccettato() {
        voto.setStato(StatoVoto.ACCETTATO);
        SegreteriaObserver observer = new SegreteriaObserver();
        observer.update(voto);

        String expected = "📋 Segreteria notificata: lo studente Mario Rossi ha accettato il voto 28 per l'esame Analisi 1";
        assertTrue(output.toString().contains(expected));
    }

    @Test
    void testSegreteriaObserver_conVotoRifiutato() {
        voto.setStato(StatoVoto.RIFIUTATO);
        SegreteriaObserver observer = new SegreteriaObserver();
        observer.update(voto);

        String expected = "📋 Segreteria notificata: lo studente Mario Rossi ha rifiutato il voto 28 per l'esame Analisi 1";
        assertTrue(output.toString().contains(expected));
    }

    @Test
    void testSegreteriaObserver_conVotoInAttesa() {
        voto.setStato(StatoVoto.ATTESA);
        SegreteriaObserver observer = new SegreteriaObserver();
        observer.update(voto);

        String expected = "📋 Segreteria notificata: lo studente Mario Rossi ha in attesa il voto 28 per l'esame Analisi 1";
        assertTrue(output.toString().contains(expected));
    }

    public PrintStream getOriginalOut() {
        return originalOut;
    }
}
