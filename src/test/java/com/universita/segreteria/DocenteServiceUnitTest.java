package com.universita.segreteria;

import com.universita.segreteria.dto.EsameDTO;
import com.universita.segreteria.dto.StudenteDTO;
import com.universita.segreteria.dto.VotoDTO;
import com.universita.segreteria.model.Esame;
import com.universita.segreteria.model.StatoVoto;
import com.universita.segreteria.model.Studente;
import com.universita.segreteria.model.Voto;
import com.universita.segreteria.notifier.VotoNotifier;
import com.universita.segreteria.repository.EsameRepository;
import com.universita.segreteria.repository.StudenteRepository;
import com.universita.segreteria.repository.VotoRepository;
import com.universita.segreteria.service.DocenteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocenteServiceUnitTest {

    @Mock private StudenteRepository studenteRepo;
    @Mock private EsameRepository esameRepo;
    @Mock private VotoRepository votoRepo;
    @Mock private VotoNotifier votoNotifier;

    @InjectMocks private DocenteService docenteService;

    private StudenteDTO studenteDTO;
    private EsameDTO esameDTO;
    private Studente studente;
    private Esame esame;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        studenteDTO = new StudenteDTO();
        studenteDTO.setMatricola("123");

        esameDTO = new EsameDTO();
        esameDTO.setNome("Matematica");

        studente = new Studente();
        studente.setMatricola("123");

        esame = new Esame();
        esame.setNome("Matematica");

        when(studenteRepo.findByMatricola("123")).thenReturn(Optional.of(studente));
        when(esameRepo.findByNome("Matematica")).thenReturn(Optional.of(esame));
        when(votoRepo.save(any(Voto.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testInserisciVoto_insufficiente() {
        // Arrange
        int votoValore = 12;
        when(votoRepo.existsByStudenteAndEsame(studente, esame)).thenReturn(false);

        // Act
        VotoDTO result = docenteService.inserisciVoto(studenteDTO, esameDTO, votoValore);

        // Assert
        assertEquals(votoValore, result.getVoto());
        verify(votoRepo).save(any(Voto.class));
        assertEquals(StatoVoto.RIFIUTATO, result.getStato());
    }

    @Test
    void testInserisciVoto_sufficiente() {
        // Arrange
        int votoValore = 24;
        when(votoRepo.existsByStudenteAndEsame(studente, esame)).thenReturn(false);

        // Act
        VotoDTO result = docenteService.inserisciVoto(studenteDTO, esameDTO, votoValore);

        // Assert
        assertEquals(votoValore, result.getVoto());
        verify(votoRepo).save(any(Voto.class));
        assertEquals(StatoVoto.ATTESA, result.getStato());
    }

    @Test
    void testInserisciVoto_studenteNonTrovato() {
        // Arrange
        when(studenteRepo.findByMatricola("123")).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(RuntimeException.class, () ->
                docenteService.inserisciVoto(studenteDTO, esameDTO, 18)
        );
    }

    @Test
    void testInserisciVoto_esameNonTrovato() {
        // Arrange
        when(esameRepo.findByNome("Matematica")).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(RuntimeException.class, () ->
                docenteService.inserisciVoto(studenteDTO, esameDTO, 18)
        );
    }

    @Test
    void testInserisciVoto_votoEsistente() {
        // Arrange
        when(studenteRepo.findByMatricola("123")).thenReturn(Optional.of(studente));
        when(esameRepo.findByNome("Matematica")).thenReturn(Optional.of(esame));
        when(votoRepo.existsByStudenteAndEsame(studente, esame)).thenReturn(true);

        // Act + Assert
        assertThrows(RuntimeException.class, () ->
                docenteService.inserisciVoto(studenteDTO, esameDTO, 18)
        );
    }

}
