package com.universita.segreteria.service;


import com.universita.segreteria.dto.StudenteDTO;
import com.universita.segreteria.model.*;
import com.universita.segreteria.repository.EsameRepository;
import com.universita.segreteria.repository.StudenteRepository;
import com.universita.segreteria.repository.VotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StudenteService
{
    private final EsameRepository esameRepo;
    private final StudenteRepository studenteRepo;
    private final VotoRepository votoRepo;

    public List<Esame> esamiSuperati(StudenteDTO studenteDTO){
        if (Objects.isNull(studenteDTO.matricola())) throw new RuntimeException("Matricola mancate, inserire matricola");

        String matricola = studenteDTO.matricola();

        Studente studente  = studenteRepo.findByMatricola(matricola).orElseThrow(() -> new RuntimeException("Matricola non valida, stundente non trovato"));

        return studente.getVoti().stream()
                .filter(v -> v.getStato() == StatoVoto.ACCETTATO)
                .map(Voto::getEsame)
                .toList();
    }

    public Esame prenotaEsame(Long studenteId, Long esameId)
    {
        Studente studente = studenteRepo.findById(studenteId).orElseThrow(()-> new RuntimeException("Studente non trovato"));
        Esame esame = esameRepo.findById(esameId).orElseThrow(()-> new RuntimeException("Esame non trovato"));
        esame.getStudentiPrenotati().add(studente);
        studente.getEsami().add(esame);
        studenteRepo.save(studente);
        return esame;
    }

    //Consulta piano di studi
    public PianoDiStudi consultaPianoStudi(Long studenteId)
    {
        Studente studente = studenteRepo.findById(studenteId).orElseThrow(()-> new RuntimeException("Studente non trovato"));
        return studente.getPianoDiStudi();
    }

    //aggiorna stato esame
    public Voto aggiornaStatoVoto(Long votoId, boolean accetta)
    {
        Voto voto = votoRepo.findById(votoId).orElseThrow(()-> new RuntimeException("Voto non assegnato"));
        if(voto.getStato() != StatoVoto.IN_ATTESA)
        {
            throw new RuntimeException("Il voto è già stato accettato o rifiutato");
        }
        voto.setStato(accetta ? StatoVoto.ACCETTATO : StatoVoto.RIFIUTATO);
        return votoRepo.save(voto);
    }
}
