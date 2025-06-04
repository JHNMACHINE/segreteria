package com.universita.segreteria.service;

import com.universita.segreteria.dto.StudenteDTO;
import com.universita.segreteria.model.*;
import com.universita.segreteria.notifier.AccettazioneNotifier;
import com.universita.segreteria.observer.SegreteriaObserver;
import com.universita.segreteria.repository.StudenteRepository;
import com.universita.segreteria.repository.VotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SegreteriaService {
    @Autowired
    private StudenteRepository studenteRepo;
    @Autowired
    private VotoRepository votoRepo;
    @Autowired
    private AccettazioneNotifier accettazioneNotifier;
    @Autowired
    private PianoStudiService pianoStudiService;

    public Studente inserisciStudente(Studente studente) {
        List<Esame> esami = pianoStudiService.getEsamiPerPiano(studente.getPianoDiStudi());

        for (Esame esame : esami) {
            Voto voto = new Voto();
            voto.setEsame(esame);
            voto.setStudente(studente);
            voto.setStato(StatoVoto.IN_ATTESA);
            studente.getVoti().add(voto);
        }

        return studenteRepo.save(studente);
    }


    public Voto confermaVoto(StudenteDTO studenteDTO, Long votoId) {
        Voto voto = votoRepo.findById(votoId)
                .orElseThrow(() -> new RuntimeException("Voto non trovato"));

        if (Objects.isNull(studenteDTO.matricola()))
            throw new RuntimeException("Matricola mancate, inserire matricola");

        String matricola = studenteDTO.matricola();

        Studente studente = studenteRepo.findByMatricola(matricola).orElseThrow(() -> new RuntimeException("Matricola non valida, studente non trovato"));

        // Verifica che il voto appartenga allo studente
        if (!voto.getStudente().getId().equals(studente.getId())) {
            throw new RuntimeException("Questo voto non appartiene allo studente");
        }

        // Cambia lo stato se necessario
        if (voto.getStato() == StatoVoto.IN_ATTESA) {
            voto.setStato(StatoVoto.ACCETTATO);
            voto = votoRepo.save(voto);
        }

        // Notifica la segreteria
        SegreteriaObserver segreteria = new SegreteriaObserver();
        accettazioneNotifier.attach(segreteria);
        accettazioneNotifier.notifyObservers(voto);
        accettazioneNotifier.detach(segreteria);

        return voto;
    }


    public List<Studente> cercaStudente(String nome, String cognome) {
        return studenteRepo.findByNomeAndCognome(nome, cognome);
    }


    public Optional<Studente> cercaStudentePerMatricola(String matricola) {
        return studenteRepo.findByMatricola(matricola);
    }

    public Studente cambiaPianoDiStudi(Long studenteId, PianoDiStudi nuovoPiano) {
        Studente studente = studenteRepo.findById(studenteId)
                .orElseThrow(() -> new RuntimeException("Studente non trovato"));
        studente.setPianoDiStudi(nuovoPiano);
        return studenteRepo.save(studente);
    }
}
