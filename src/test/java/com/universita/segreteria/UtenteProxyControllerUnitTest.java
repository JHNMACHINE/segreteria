package com.universita.segreteria;

import com.universita.segreteria.controller.UtenteProxyController;
import com.universita.segreteria.dto.RichiestaOperazione;
import com.universita.segreteria.model.TipoUtente;
import com.universita.segreteria.service.UserServiceProxy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UtenteProxyControllerUnitTest {

    @Mock
    private UserServiceProxy userServiceProxy;

    @Mock
    private SecurityContext securityContext;

    @Mock
    Authentication authentication;

    @InjectMocks
    private UtenteProxyController controller;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        // Setup SecurityContextHolder mock
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
        SecurityContextHolder.clearContext();
    }

    @Test
    void testEsegui_operazioneConRuoloStudente() {
        // 1. Crea un'autorità concreta (non lambda)
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_STUDENTE");

        // 2. Crea una collezione concreta
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(authority);

        // 3. Mock esplicito senza wildcard
        when(authentication.getAuthorities()).thenAnswer(invocation -> authorities);

        // Preparo richiesta e risultato
        RichiestaOperazione richiesta = new RichiestaOperazione();
        richiesta.setNomeOperazione("prenotaEsame");
        richiesta.setParametri(new Object[]{1L, 2L});

        Object expectedResult = "ok";
        when(userServiceProxy.eseguiOperazione("prenotaEsame", 1L, 2L)).thenReturn(expectedResult);

        ResponseEntity<?> response = controller.esegui(richiesta);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(expectedResult, response.getBody());

        // Verifico che il ruolo sia stato impostato correttamente
        verify(userServiceProxy).setRuolo(TipoUtente.STUDENTE);
        verify(userServiceProxy).eseguiOperazione("prenotaEsame", 1L, 2L);
    }

    @Test
    void testEsegui_senzaRuolo_throwsSecurityException() {
        when(authentication.getAuthorities()).thenReturn(Collections.emptySet());

        RichiestaOperazione richiesta = new RichiestaOperazione();
        richiesta.setNomeOperazione("qualunqueOperazione");
        richiesta.setParametri(new Object[]{});

        SecurityException exception = assertThrows(SecurityException.class, () -> controller.esegui(richiesta));
        assertEquals("Ruolo non disponibile", exception.getMessage());

        // Nessuna chiamata a userServiceProxy deve essere fatta
        verifyNoInteractions(userServiceProxy);
    }

    @Test
    void testEsegui_operazioneConRuoloDocente() {
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_DOCENTE");

        // 2. Crea una collezione concreta
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(authority);

        when(authentication.getAuthorities()).thenAnswer(invocation -> authorities);

        RichiestaOperazione richiesta = new RichiestaOperazione();
        richiesta.setNomeOperazione("inserisciVoto");
        richiesta.setParametri(new Object[]{"studente1", "esame1", 30});

        Object expectedResult = "Voto inserito";
        when(userServiceProxy.eseguiOperazione("inserisciVoto", "studente1", "esame1", 30)).thenReturn(expectedResult);

        ResponseEntity<?> response = controller.esegui(richiesta);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(expectedResult, response.getBody());
        verify(userServiceProxy).setRuolo(TipoUtente.DOCENTE);
        verify(userServiceProxy).eseguiOperazione("inserisciVoto", "studente1", "esame1", 30);
    }

    @Test
    void testEsegui_operazioneConRuoloSegretario_eParametroComplesso() {
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_SEGRETARIO");
        // 2. Crea una collezione concreta
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(authority);
        when(authentication.getAuthorities()).thenAnswer(invocation -> authorities);

        Map<String, Object> studenteDTO = new HashMap<>();
        studenteDTO.put("nome", "Mario");
        studenteDTO.put("matricola", "123456");

        RichiestaOperazione richiesta = new RichiestaOperazione();
        richiesta.setNomeOperazione("inserisciStudente");
        richiesta.setParametri(new Object[]{studenteDTO});

        Object expectedResult = "OK";
        when(userServiceProxy.eseguiOperazione("inserisciStudente", studenteDTO)).thenReturn(expectedResult);

        ResponseEntity<?> response = controller.esegui(richiesta);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(expectedResult, response.getBody());
        verify(userServiceProxy).setRuolo(TipoUtente.SEGRETARIO);
        verify(userServiceProxy).eseguiOperazione("inserisciStudente", studenteDTO);
    }

    @Test
    void testEsegui_ruoloInvalido_throwsIllegalArgumentException() {
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_UNKNOWN");
        // 2. Crea una collezione concreta
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(authority);
        when(authentication.getAuthorities()).thenAnswer(invocation -> authorities);

        RichiestaOperazione richiesta = new RichiestaOperazione();
        richiesta.setNomeOperazione("qualunque");
        richiesta.setParametri(new Object[]{});

        assertThrows(IllegalArgumentException.class, () -> controller.esegui(richiesta));
        verify(userServiceProxy, never()).eseguiOperazione(anyString(), any());
    }


}

