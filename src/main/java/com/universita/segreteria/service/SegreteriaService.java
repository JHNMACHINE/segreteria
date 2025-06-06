package com.universita.segreteria.service;

import com.universita.segreteria.dto.DocenteDTO;
import com.universita.segreteria.dto.StudenteDTO;
import com.universita.segreteria.dto.VotoDTO;
import com.universita.segreteria.mapper.DocenteMapper;
import com.universita.segreteria.mapper.StudentMapper;
import com.universita.segreteria.mapper.VotoMapper;
import com.universita.segreteria.model.*;
import com.universita.segreteria.notifier.AccettazioneNotifier;
import com.universita.segreteria.observer.SegreteriaObserver;
import com.universita.segreteria.repository.DocenteRepository;
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
    @Autowired
    private DocenteRepository docenteRepository;

    public StudenteDTO inserisciStudente(StudenteDTO studenteDTO) {
        Studente studente = studenteRepo.findByMatricola(studenteDTO.getMatricola()).orElseThrow(() -> new RuntimeException("Matricola non valida, studente non trovato"));
        List<Esame> esami = pianoStudiService.getEsamiPerPiano(studente.getPianoDiStudi());

        studente.setEsami(esami);
        studenteRepo.save(studente);
        return StudentMapper.convertiStudenteInDTO(studente);
    }

    public DocenteDTO inserisciDocente(DocenteDTO docenteDTO) {
        Docente docente = DocenteMapper.fromDTO(docenteDTO);
        Docente saved = docenteRepository.save(docente);
        return DocenteMapper.toDTO(saved);
    }


    public VotoDTO confermaVoto(StudenteDTO studenteDTO, Long votoId) {
        Voto voto = votoRepo.findById(votoId).orElseThrow(() -> new RuntimeException("Voto non trovato"));

        if (Objects.isNull(studenteDTO.getMatricola()))
            throw new RuntimeException("Matricola mancate, inserire matricola");

        String matricola = studenteDTO.getMatricola();

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

        return VotoMapper.convertiInDTO(voto);
    }


    public List<StudenteDTO> cercaStudente(String nome, String cognome) {
        List<Studente> studenti = studenteRepo.findByNomeAndCognome(nome, cognome);
        return StudentMapper.convertListStudentiToDTO(studenti);
    }


    public StudenteDTO cercaStudentePerMatricola(String matricola) {
        Studente studente = studenteRepo.findByMatricola(matricola).orElseThrow(() -> new RuntimeException("Matricola non valida, studente non trovato"));
        return StudentMapper.convertiStudenteInDTO(studente);
    }

    public StudenteDTO cambiaPianoDiStudi(Long studenteId, PianoDiStudi nuovoPiano) {
        Studente studente = studenteRepo.findById(studenteId).orElseThrow(() -> new RuntimeException("Studente non trovato"));
        studente.setPianoDiStudi(nuovoPiano);
        studenteRepo.save(studente);
        return StudentMapper.convertiStudenteInDTO(studente);
    }
}
