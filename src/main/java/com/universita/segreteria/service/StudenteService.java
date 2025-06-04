package com.universita.segreteria.service;


import com.universita.segreteria.dto.EsameDTO;
import com.universita.segreteria.dto.StudenteDTO;
import com.universita.segreteria.mapper.EsameMapper;
import com.universita.segreteria.model.*;
import com.universita.segreteria.repository.EsameRepository;
import com.universita.segreteria.repository.StudenteRepository;
import com.universita.segreteria.repository.VotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudenteService {
    @Autowired
    private EsameRepository esameRepo;
    @Autowired
    private StudenteRepository studenteRepo;
    @Autowired
    private VotoRepository votoRepo;
    @Autowired
    private PianoStudiService pianoStudiService;

    public List<EsameDTO> esamiPrenotabili(StudenteDTO studenteDTO) {
        Studente studente = studenteRepo.findByMatricola(studenteDTO.getMatricola()).orElseThrow(() -> new RuntimeException("Studente non trovato"));

        List<Voto> voti = studente.getVoti();
        List<Esame> superati = voti.stream().filter(v -> v.getStato() == StatoVoto.ACCETTATO).map(Voto::getEsame).toList();

        List<Esame> giaValutati = voti.stream().map(Voto::getEsame).toList();

        List<Esame> giaPrenotati = studente.getEsami(); // quelli già prenotati
        PianoDiStudi piano = studente.getPianoDiStudi();
        LocalDate oggi = LocalDate.now();
        List<Esame> finalList = pianoStudiService.getEsamiPerPiano(piano).stream().filter(e -> !superati.contains(e))               // non già superato
                .filter(e -> !giaValutati.contains(e))            // non già valutato
                .filter(e -> !giaPrenotati.contains(e))           // non già prenotato
                .filter(e -> e.getDate().isAfter(oggi))           // data futura
                .toList();
        return EsameMapper.convertListEsamiToDTO(finalList);
    }


    public List<EsameDTO> esamiSuperati(StudenteDTO studenteDTO) {
        if (Objects.isNull(studenteDTO.getMatricola()))
            throw new RuntimeException("Matricola mancante, inserire matricola");

        String matricola = studenteDTO.getMatricola();

        Studente studente = studenteRepo.findByMatricola(matricola).orElseThrow(() -> new RuntimeException("Matricola non valida, studente non trovato"));
        List<Esame> esami = studente.getVoti().stream().filter(v -> v.getStato() == StatoVoto.ACCETTATO).map(Voto::getEsame).toList();
        return EsameMapper.convertListEsamiToDTO(esami);
    }

    public EsameDTO prenotaEsame(Long studenteId, Long esameId) {
        Studente studente = studenteRepo.findById(studenteId).orElseThrow(() -> new RuntimeException("Studente non trovato"));
        Esame esame = esameRepo.findById(esameId).orElseThrow(() -> new RuntimeException("Esame non trovato"));
        esame.getStudentiPrenotati().add(studente);
        studente.getEsami().add(esame);
        studenteRepo.save(studente);
        return EsameMapper.toDTO(esame);
    }

    public PianoDiStudi consultaPianoStudi(Long studenteId) {
        Studente studente = studenteRepo.findById(studenteId).orElseThrow(() -> new RuntimeException("Studente non trovato"));
        return studente.getPianoDiStudi();
    }

    public Voto aggiornaStatoVoto(Long votoId, boolean accetta) {
        Voto voto = votoRepo.findById(votoId).orElseThrow(() -> new RuntimeException("Voto non assegnato"));
        if (voto.getStato() != StatoVoto.IN_ATTESA) {
            throw new RuntimeException("Il voto è già stato accettato o rifiutato");
        }
        voto.setStato(accetta ? StatoVoto.ACCETTATO : StatoVoto.RIFIUTATO);
        return votoRepo.save(voto);
    }

    public List<Esame> getEsamiDaSostenere(StudenteDTO studenteDTO) {
        String matricola = studenteDTO.getMatricola();

        Studente studente = studenteRepo.findByMatricola(matricola).orElseThrow(() -> new RuntimeException("Matricola non valida, studente non trovato"));

        return studente.getVoti().stream().filter(v -> v.getStato() != StatoVoto.ACCETTATO).map(Voto::getEsame).collect(Collectors.toList());
    }
}
