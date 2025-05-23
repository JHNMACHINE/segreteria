package com.universita.segreteria.service;

import com.universita.segreteria.model.PianoDiStudi;
import com.universita.segreteria.model.Studente;
import com.universita.segreteria.model.Voto;
import com.universita.segreteria.repository.StudenteRepository;
import com.universita.segreteria.repository.VotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SegreteriaService {
    private final StudenteRepository studenteRepo;
    private final VotoRepository votoRepo;

    public Studente inserisciStudente(Studente studente) {
        return studenteRepo.save(studente);
    }

    public Voto confermaVoto(Long votoId) {
        Voto voto = votoRepo.findById(votoId)
                .orElseThrow(() -> new RuntimeException("Voto non trovato"));
        return votoRepo.save(voto);
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
