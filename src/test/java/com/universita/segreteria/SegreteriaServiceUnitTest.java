package com.universita.segreteria;

import com.universita.segreteria.dto.DocenteDTO;
import com.universita.segreteria.dto.StudenteDTO;
import com.universita.segreteria.dto.VotoDTO;
import com.universita.segreteria.mapper.DocenteMapper;
import com.universita.segreteria.model.*;
import com.universita.segreteria.notifier.AcceptationNotifier;
import com.universita.segreteria.observer.SegreteriaObserver;
import com.universita.segreteria.repository.DocenteRepository;
import com.universita.segreteria.repository.StudenteRepository;
import com.universita.segreteria.repository.VotoRepository;
import com.universita.segreteria.service.PianoStudioService;
import com.universita.segreteria.service.SegreteriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SegreteriaServiceUnitTest {

	@Mock
	private StudenteRepository studenteRepo;
	@Mock
	private VotoRepository votoRepo;
	@Mock
	private AcceptationNotifier acceptationNotifier;
	@Mock
	private PianoStudioService pianoStudioService;
	@Mock
	private DocenteRepository docenteRepository;

	@InjectMocks
	private SegreteriaService segreteriaService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testInserisciStudente() {
		StudenteDTO dto = new StudenteDTO();
		dto.setMatricola("MAT123");

		Studente studente = new Studente();
		studente.setMatricola("MAT123");
		studente.setPianoDiStudi(PianoDiStudi.INGEGNERIA);

		List<Esame> esami = List.of(new Esame());

		when(studenteRepo.findByMatricola("MAT123")).thenReturn(Optional.of(studente));
		when(pianoStudioService.getEsamiPerPiano(PianoDiStudi.INGEGNERIA)).thenReturn(esami);
		when(studenteRepo.save(any())).thenReturn(studente);

		StudenteDTO result = segreteriaService.inserisciStudente(dto);

		assertNotNull(result);
		verify(studenteRepo).save(studente);
	}

	@Test
	void testInserisciDocente() {
		DocenteDTO dto = new DocenteDTO();
		dto.setNome("Mario");
		dto.setCognome("Rossi");

		Docente docente = DocenteMapper.fromDTO(dto);
		when(docenteRepository.save(any())).thenReturn(docente);

		DocenteDTO result = segreteriaService.inserisciDocente(dto);

		assertEquals(dto.getNome(), result.getNome());
		assertEquals(dto.getCognome(), result.getCognome());
		verify(docenteRepository).save(any());
	}

	@Test
	void testConfermaVoto_scenarioCorretto() {
		StudenteDTO dto = new StudenteDTO();
		dto.setMatricola("MAT123");

		Studente studente = new Studente();
		studente.setId(1L);
		studente.setMatricola("MAT123");

		Esame esame = new Esame();
		esame.setId(100L);
		esame.setNome("Analisi Matematica I");

		Voto voto = new Voto();
		voto.setId(10L);
		voto.setStato(StatoVoto.ATTESA);
		voto.setStudente(studente);
		voto.setEsame(esame);
		voto.setVoto(30);

		when(votoRepo.findById(10L)).thenReturn(Optional.of(voto));
		when(studenteRepo.findByMatricola("MAT123")).thenReturn(Optional.of(studente));
		when(votoRepo.save(any())).thenReturn(voto);

		VotoDTO result = segreteriaService.confermaVoto(dto, 10L);

		assertEquals(StatoVoto.ACCETTATO, voto.getStato());
		assertNotNull(result);
		assertEquals("Analisi Matematica I", result.getEsameNome());
		verify(votoRepo).save(voto);
		verify(acceptationNotifier).attach(any(SegreteriaObserver.class));
		verify(acceptationNotifier).notifyObservers(voto);
		verify(acceptationNotifier).detach(any(SegreteriaObserver.class));
	}


	@Test
	void testConfermaVoto_matricolaMancante() {
		StudenteDTO dto = new StudenteDTO(); // senza matricola
		assertThrows(RuntimeException.class, () -> segreteriaService.confermaVoto(dto, 1L));
	}

	@Test
	void testConfermaVoto_votoNonAppartieneAStudente() {
		StudenteDTO dto = new StudenteDTO();
		dto.setMatricola("MAT456");

		Studente studenteRichiedente = new Studente();
		studenteRichiedente.setId(2L);

		Studente proprietarioVoto = new Studente();
		proprietarioVoto.setId(99L);

		Voto voto = new Voto();
		voto.setId(1L);
		voto.setStato(StatoVoto.ATTESA);
		voto.setStudente(proprietarioVoto);

		when(votoRepo.findById(1L)).thenReturn(Optional.of(voto));
		when(studenteRepo.findByMatricola("MAT456")).thenReturn(Optional.of(studenteRichiedente));

		RuntimeException ex = assertThrows(RuntimeException.class, () -> segreteriaService.confermaVoto(dto, 1L));
		assertTrue(ex.getMessage().contains("non appartiene"));
	}

	@Test
	void testCercaStudente() {
		Studente studente = new Studente();
		studente.setNome("Luca");
		studente.setCognome("Verdi");

		when(studenteRepo.findByNomeAndCognome("Luca", "Verdi")).thenReturn(List.of(studente));

		List<StudenteDTO> result = segreteriaService.cercaStudente("Luca", "Verdi");

		assertEquals(1, result.size());
		assertEquals("Luca", result.getFirst().getNome());
	}

	@Test
	void testCercaStudentePerMatricola() {
		Studente studente = new Studente();
		studente.setMatricola("MAT789");

		when(studenteRepo.findByMatricola("MAT789")).thenReturn(Optional.of(studente));

		StudenteDTO result = segreteriaService.cercaStudentePerMatricola("MAT789");

		assertEquals("MAT789", result.getMatricola());
	}

	@Test
	void testCambiaPianoDiStudi() {
		Studente studente = new Studente();
		studente.setId(1L);
		studente.setPianoDiStudi(PianoDiStudi.GIURISPRUDENZA);

		when(studenteRepo.findById(1L)).thenReturn(Optional.of(studente));
		when(studenteRepo.save(any())).thenReturn(studente);

		StudenteDTO result = segreteriaService.cambiaPianoDiStudi(1, PianoDiStudi.MEDICINA.toString());

		assertEquals(PianoDiStudi.MEDICINA, result.getPianoDiStudi());
		verify(studenteRepo).save(studente);
	}
}
