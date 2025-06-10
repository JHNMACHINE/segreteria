package com.universita.segreteria;

import com.universita.segreteria.dto.EsameDTO;
import com.universita.segreteria.dto.StudenteDTO;
import com.universita.segreteria.model.*;
import com.universita.segreteria.repository.StudenteRepository;
import com.universita.segreteria.service.PianoStudiService;
import com.universita.segreteria.service.StudenteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class StudenteServiceUnitTest {

    @Mock
    private StudenteRepository studenteRepo;

    @Mock
    private PianoStudiService pianoStudiService;

    @InjectMocks
    private StudenteService studenteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Test 1: Esami nel passato non devono essere prenotabili
    @Test
    void testEsamiPrenotabili_esamiNelPassatoNonSonoPrenotabili() {
        // Arrange
        StudenteDTO studenteDTO = buildStudenteDTO("MAT123");
        Studente studente = buildStudenteConPianoEsenzaVotiOEsami();
        Esame esamePassato = buildEsame(1L, "EsamePassato", LocalDate.now().minusDays(1));
        List<Esame> esamiPiano = Collections.singletonList(esamePassato);
        mockStudenteConEsami("MAT123", studente, esamiPiano);

        // Act
        List<EsameDTO> result = studenteService.esamiPrenotabili(studenteDTO);

        // Assert
        assertTrue(result.isEmpty(), "Esami nel passato non devono essere prenotabili");
    }

    // Test 2: Esami già superati non devono essere prenotabili
    @Test
    void testEsamiPrenotabili_esamiSuperatiNonSonoPrenotabili() {
        // Arrange
        StudenteDTO studenteDTO = buildStudenteDTO("MAT123");
        Studente studente = buildStudenteConPianoEsenzaVotiOEsami();

        Esame esameSuperato = buildEsame(2L, "EsameSuperato", LocalDate.now().plusDays(5));
        Voto votoSuperato = buildVoto(esameSuperato, StatoVoto.ACCETTATO);
        studente.setVoti(List.of(votoSuperato));

        List<Esame> esamiPiano = Collections.singletonList(esameSuperato);
        mockStudenteConEsami("MAT123", studente, esamiPiano);

        // Act
        List<EsameDTO> result = studenteService.esamiPrenotabili(studenteDTO);

        // Assert
        assertTrue(result.isEmpty(), "Esami già superati non devono essere prenotabili");
    }

    // Test 3: Esami già prenotati non devono essere prenotabili
    @Test
    void testEsamiPrenotabili_esamiGiaPrenotatiNonSonoPrenotabili() {
        // Arrange
        StudenteDTO studenteDTO = buildStudenteDTO("MAT123");
        Studente studente = buildStudenteConPianoEsenzaVotiOEsami();

        Esame esamePrenotato = buildEsame(3L, "EsamePrenotato", LocalDate.now().plusDays(10));
        // Simuliamo che l'esame risulti già prenotato dallo studente
        studente.setEsami(List.of(esamePrenotato));

        List<Esame> esamiPiano = List.of(esamePrenotato);
        mockStudenteConEsami("MAT123", studente, esamiPiano);

        // Act
        List<EsameDTO> result = studenteService.esamiPrenotabili(studenteDTO);

        // Assert
        assertTrue(result.isEmpty(), "Esami già prenotati non devono essere prenotabili");
    }

    // Test 4: Esami futuri non superati e non prenotati sono prenotabili
    @Test
    void testEsamiPrenotabili_esamiFuturiNonSuperatiENonPrenotatiSonoPrenotabili() {
        // Arrange
        StudenteDTO studenteDTO = buildStudenteDTO("MAT123");
        Studente studente = buildStudenteConPianoEsenzaVotiOEsami();

        Esame esameDisponibile = buildEsame(4L, "EsameDisponibile", LocalDate.now().plusDays(15));
        List<Esame> esamiPiano = Collections.singletonList(esameDisponibile);
        mockStudenteConEsami("MAT123", studente, esamiPiano);

        // Act
        List<EsameDTO> result = studenteService.esamiPrenotabili(studenteDTO);

        // Assert
        assertEquals(1, result.size(), "Un esame valido dovrebbe essere prenotabile");
        EsameDTO dtoRimasto = result.getFirst();
        assertEquals(4L, dtoRimasto.getId());
        assertEquals("EsameDisponibile", dtoRimasto.getNome());
        assertEquals(esameDisponibile.getDate(), dtoRimasto.getDate());
        assertNull(dtoRimasto.getStatoEsame());
        assertNull(dtoRimasto.getDocenteId());
    }

    // -----------------------
    // Metodi di supporto
    // -----------------------

    private StudenteDTO buildStudenteDTO(String matricola) {
        StudenteDTO dto = new StudenteDTO();
        dto.setMatricola(matricola);
        return dto;
    }

    private Esame buildEsame(Long id, String nome, LocalDate data) {
        return Esame.builder()
                .id(id)
                .nome(nome)
                .date(data)
                .build();
    }

    private Voto buildVoto(Esame esame, StatoVoto stato) {
        Voto voto = new Voto();
        voto.setEsame(esame);
        voto.setStato(stato);
        return voto;
    }

    private Studente buildStudenteConPianoEsenzaVotiOEsami() {
        Studente studente = new Studente();
        studente.setPianoDiStudi(PianoDiStudi.values()[0]);
        studente.setVoti(List.of());
        studente.setEsami(List.of());
        return studente;
    }

    private void mockStudenteConEsami(String matricola, Studente studente, List<Esame> esamiPiano) {
        when(studenteRepo.findByMatricola(matricola)).thenReturn(Optional.of(studente));
        when(pianoStudiService.getEsamiPerPiano(studente.getPianoDiStudi())).thenReturn(esamiPiano);
    }
}
