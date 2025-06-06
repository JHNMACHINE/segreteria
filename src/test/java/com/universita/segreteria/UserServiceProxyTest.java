package com.universita.segreteria;

import com.universita.segreteria.dto.EsameDTO;
import com.universita.segreteria.dto.StudenteDTO;
import com.universita.segreteria.dto.VotoDTO;
import com.universita.segreteria.model.PianoDiStudi;
import com.universita.segreteria.model.TipoUtente;
import com.universita.segreteria.service.DocenteService;
import com.universita.segreteria.service.SegreteriaService;
import com.universita.segreteria.service.StudenteService;
import com.universita.segreteria.service.UserServiceProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceProxyTest {

    @Mock
    private SegreteriaService segreteriaService;

    @Mock
    private StudenteService studenteService;

    @Mock
    private DocenteService docenteService;

    @InjectMocks
    private UserServiceProxy userServiceProxy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Impostiamo ruolo di default
        userServiceProxy.setRuolo(TipoUtente.SEGRETARIO);
    }

    @Test
    void testSegreteria_inserisciStudente() {
        StudenteDTO dto = new StudenteDTO();
        when(segreteriaService.inserisciStudente(dto)).thenReturn(dto);

        Object result = userServiceProxy.eseguiOperazione("inserisciStudente", dto);

        assertEquals(dto, result);
        verify(segreteriaService).inserisciStudente(dto);
    }

    @Test
    void testStudente_prenotaEsame() {
        userServiceProxy.setRuolo(TipoUtente.STUDENTE);
        EsameDTO esameDTO = new EsameDTO();
        when(studenteService.prenotaEsame(1L, 2L)).thenReturn(esameDTO);

        Object result = userServiceProxy.eseguiOperazione("prenotaEsame", 1L, 2L);

        assertEquals(esameDTO, result);
        verify(studenteService).prenotaEsame(1L, 2L);
    }

    @Test
    void testDocente_inserisciVoto() {
        userServiceProxy.setRuolo(TipoUtente.DOCENTE);
        StudenteDTO studenteDTO = new StudenteDTO();
        EsameDTO esameDTO = new EsameDTO();
        VotoDTO votoDTO = new VotoDTO();
        when(docenteService.inserisciVoto(studenteDTO, esameDTO, 30)).thenReturn(votoDTO);

        Object result = userServiceProxy.eseguiOperazione("inserisciVoto", studenteDTO, esameDTO, 30);

        assertEquals(votoDTO, result);
        verify(docenteService).inserisciVoto(studenteDTO, esameDTO, 30);
    }

    @Test
    void testSegreteria_operazioneNonSupportata() {
        Exception exception = assertThrows(UnsupportedOperationException.class, () ->
                userServiceProxy.eseguiOperazione("operazioneInvalida"));

        assertEquals("Operazione non consentita per la segreteria.", exception.getMessage());
    }

    @Test
    void testDocente_operazioneNonSupportata() {
        userServiceProxy.setRuolo(TipoUtente.DOCENTE);

        Exception exception = assertThrows(UnsupportedOperationException.class, () ->
                userServiceProxy.eseguiOperazione("operazioneInvalida"));

        assertEquals("Operazione non consentita per il docente.", exception.getMessage());
    }

    @Test
    void testStudente_cambiaPianoDiStudi_nonConsentita() {
        userServiceProxy.setRuolo(TipoUtente.STUDENTE);
        PianoDiStudi piano = PianoDiStudi.INFORMATICA; // o qualunque valore valido

        Exception exception = assertThrows(UnsupportedOperationException.class, () ->
                userServiceProxy.eseguiOperazione("cambiaPianoDiStudi", 1L, piano));

        assertEquals("Operazione non consentita per lo studente.", exception.getMessage());
    }
}
