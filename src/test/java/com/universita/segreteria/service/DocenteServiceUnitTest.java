package com.universita.segreteria.service;

import com.universita.segreteria.dto.EsameDTO;
import com.universita.segreteria.dto.StudenteDTO;
import com.universita.segreteria.dto.VotoDTO;
import com.universita.segreteria.model.StatoVoto;
import com.universita.segreteria.mapper.VotoMapper;
import com.universita.segreteria.model.Esame;
import com.universita.segreteria.model.Studente;
import com.universita.segreteria.model.Voto;
import com.universita.segreteria.notifier.VotoNotifier;
import com.universita.segreteria.observer.StudenteObserver;
import com.universita.segreteria.repository.EsameRepository;
import com.universita.segreteria.repository.StudenteRepository;
import com.universita.segreteria.repository.VotoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocenteServiceTest {

    @Mock
    private StudenteRepository studenteRepo;

    @Mock
    private EsameRepository esameRepo;

    @Mock
    private VotoRepository votoRepo;

    @Mock
    private VotoNotifier votoNotifier;

    @InjectMocks
    private DocenteService docenteService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInserisciVoto_success() {
        // Arrange
        StudenteDTO studenteDTO = new StudenteDTO();
        studenteDTO.setMatricola("123");

        EsameDTO esameDTO = new EsameDTO();
        esameDTO.setNome("Matematica");

        Studente studente = new Studente();
        studente.setMatricola("123");

        Esame esame = new Esame();
        esame.setNome("Matematica");

        when(studenteRepo.findByMatricola("123")).thenReturn(Optional.of(studente));
        when(esameRepo.findByNome("Matematica")).thenReturn(Optional.of(esame));
        when(votoRepo.existsByStudenteAndEsame(studente, esame)).thenReturn(false);

        ArgumentCaptor<Voto> votoCaptor = ArgumentCaptor.forClass(Voto.class);
        when(votoRepo.save(any(Voto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        VotoDTO result = docenteService.inserisciVoto(studenteDTO, esameDTO, 12);

        if(result.getVoto() > 0)
        {
            verify(votoRepo).save(votoCaptor.capture());
            Voto votoSalvato = votoCaptor.getValue();

            if(result.getVoto() < 18)
            {
                System.out.println("Lo studente con matricola " +votoSalvato.getStudente().getMatricola()+ "non ha superato l'esame di " +votoSalvato.getEsame().getNome()+
                        " con valutazione " +votoSalvato.getVoto());
            }
            else
            {
                System.out.println("Lo studente con matricola " +votoSalvato.getStudente().getMatricola()+ "ha superato l'esame di " +votoSalvato.getEsame().getNome()+
                        " con valutazione " +votoSalvato.getVoto());
            }
            assertEquals(12, votoSalvato.getVoto());
            assertEquals(StatoVoto.RIFIUTATO, votoSalvato.getStato());
        }

        // Assert

    }
}
