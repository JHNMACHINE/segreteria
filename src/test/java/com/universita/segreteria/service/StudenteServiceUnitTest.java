package com.universita.segreteria.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.universita.segreteria.dto.EsameDTO;
import com.universita.segreteria.dto.StudenteDTO;
import com.universita.segreteria.model.Esame;
import com.universita.segreteria.model.PianoDiStudi;
import com.universita.segreteria.model.StatoVoto;
import com.universita.segreteria.model.Voto;
import com.universita.segreteria.model.Studente;
import com.universita.segreteria.repository.StudenteRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test “unitario” di StudenteService.esamiPrenotabili(...).
 * Non carica Spring: usa solo Mockito per mockare StudenteRepository e PianoStudiService.
 */
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

    @Test
    void testEsamiPrenotabili_filtraCorrettamente() {
        System.out.println("▶ Inizio testEsamiPrenotabili_filtraCorrettamente");

        // 1) Preparo il DTO in ingresso
        StudenteDTO studenteDTO = new StudenteDTO();
        studenteDTO.setMatricola("MAT123");
        System.out.println("   • Creazione StudenteDTO con matricola "+studenteDTO.getMatricola());

        // 2) Costruisco l’entità Studente (che il mock restituirà)
        Studente studenteEntity = new Studente();
        studenteEntity.setMatricola("MAT123");
        studenteEntity.setVoti(List.of());
        studenteEntity.setEsami(List.of());
        System.out.println("   • Costruito Studente entity (voti ed esami inizialmente vuoti)");

        // 3) Creo vari Esami, con date nel passato e nel futuro
        Esame esamePassato = Esame.builder()
                .id(1L)
                .nome("EsamePassato")
                .date(LocalDate.now().minusDays(3))
                .build();
        System.out.println("   • Esame ‘EsamePassato’ con date = " + esamePassato.getDate());

        Esame esameSuperato = Esame.builder()
                .id(2L)
                .nome("EsameSuperato")
                .date(LocalDate.now().plusDays(10))
                .build();
        System.out.println("   • Esame ‘EsameSuperato’ con date = " + esameSuperato.getDate());

        Esame esameValutato = Esame.builder()
                .id(3L)
                .nome("EsameValutato")
                .date(LocalDate.now().plusDays(15))
                .build();
        System.out.println("   • Esame ‘EsameValutato’ con date = " + esameValutato.getDate());

        Esame esamePrenotato = Esame.builder()
                .id(4L)
                .nome("EsamePrenotato")
                .date(LocalDate.now().plusDays(20))
                .build();
        System.out.println("   • Esame ‘EsamePrenotato’ con date = " + esamePrenotato.getDate());

        Esame esameDisponibile = Esame.builder()
                .id(5L)
                .nome("EsameDisponibile")
                .date(LocalDate.now().plusDays(30))
                .build();
        System.out.println("   • Esame ‘EsameDisponibile’ con date = " + esameDisponibile.getDate());

        List<Esame> tuttiGliEsamiDelPiano = Arrays.asList(
                esamePassato,
                esameSuperato,
                esameValutato,
                esamePrenotato,
                esameDisponibile
        );

        // 4) Creo due Voto: uno ACCETTATO (superato) e uno RIFIUTATO (valutato)
        Voto votoSuperato = new Voto();
        votoSuperato.setStato(StatoVoto.ACCETTATO);
        votoSuperato.setEsame(esameSuperato);
        System.out.println("   • VotoSuperato creato per ‘EsameSuperato’");

        Voto votoNonSuperato = new Voto();
        votoNonSuperato.setStato(StatoVoto.RIFIUTATO);
        votoNonSuperato.setEsame(esameValutato);
        System.out.println("   • VotoNonSuperato creato per ‘EsameValutato’");

        studenteEntity.setVoti(Arrays.asList(votoSuperato, votoNonSuperato));
        System.out.println("   • Associati voti allo Studente");

        // 5) Imposto l’elenco di esami già prenotati per lo studente
        studenteEntity.setEsami(List.of(esamePrenotato));
        System.out.println("   • Aggiunto ‘EsamePrenotato’ fra gli esami già prenotati");

        // 6) Imposto il valore di PianoDiStudi (enum) associato allo studente
        PianoDiStudi pianoEnum = PianoDiStudi.values()[0];
        studenteEntity.setPianoDiStudi(pianoEnum);
        System.out.println("   • Impostato PianoDiStudi = " + pianoEnum);

        // 7) Configuro i comportamenti dei mock
        when(studenteRepo.findByMatricola("MAT123")).thenReturn(Optional.of(studenteEntity));
        System.out.println("   • Mockato studenteRepo.findByMatricola(...)");
        when(pianoStudiService.getEsamiPerPiano(pianoEnum)).thenReturn(tuttiGliEsamiDelPiano);
        System.out.println("   • Mockato pianoStudiService.getEsamiPerPiano(...)");

        // 8) Invoco il metodo da testare
        List<EsameDTO> result = studenteService.esamiPrenotabili(studenteDTO);
        System.out.println("   • Chiamato studenteService.esamiPrenotabili(...) – dimensione lista: " + result.size());

        // 9) Verifico che rimanga solo “esameDisponibile”
        assertEquals(1, result.size(), "Deve rimanere un solo esame prenotabile");
        EsameDTO dtoRimasto = result.get(0);
        System.out.println("   • DTO rimasto: id=" + dtoRimasto.getId() + ", nome=" + dtoRimasto.getNome());

        assertEquals(5L, dtoRimasto.getId());
        assertEquals("EsameDisponibile", dtoRimasto.getNome());
        assertEquals(esameDisponibile.getDate(), dtoRimasto.getDate());
        assertNull(dtoRimasto.getStatoEsame());
        assertNull(dtoRimasto.getDocenteId());
        System.out.println("   • Asserzioni finali superate correttamente");

        // 10) Verifico l’invocazione dei mock
        verify(studenteRepo, times(1)).findByMatricola("MAT123");
        verify(pianoStudiService, times(1)).getEsamiPerPiano(pianoEnum);
        System.out.println("   • Verificato invocation count dei mock");

        System.out.println("◀ Fine testEsamiPrenotabili_filtraCorrettamente");
    }

}
