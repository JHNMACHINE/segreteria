package com.universita.segreteria.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universita.segreteria.dto.RichiestaOperazione;
import com.universita.segreteria.model.TipoUtente;
import com.universita.segreteria.service.UserServiceProxy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UtenteProxyController {

    private final UserServiceProxy utenteProxy;
    private static final Logger log = LoggerFactory.getLogger(UtenteProxyController.class);

    private TipoUtente getRuoloDaContesto() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            log.error("Autenticazione nulla nel contesto di sicurezza");
            throw new SecurityException("Ruolo non disponibile: autenticazione nulla");
        }
        if (auth.getAuthorities() == null || auth.getAuthorities().isEmpty()) {
            log.error("Nessuna authority trovata per l'utente autenticato: {}", auth.getName());
            throw new SecurityException("Ruolo non disponibile: nessuna authority");
        }

        log.info("Authorities trovate per utente {}: {}", auth.getName(), auth.getAuthorities());

        for (GrantedAuthority authority : auth.getAuthorities()) {
            String nome = authority.getAuthority(); // es. ROLE_STUDENTE
            log.debug("Controllo authority: {}", nome);
            if (nome.startsWith("ROLE_")) {
                String ruolo = nome.substring("ROLE_".length());
                try {
                    TipoUtente tipo = TipoUtente.valueOf(ruolo);
                    log.info("Ruolo valido estratto: {}", tipo);
                    return tipo;
                } catch (IllegalArgumentException e) {
                    log.warn("Ruolo nel token non valido per TipoUtente: {}", ruolo);
                }
            } else {
                log.debug("Authority non con prefisso ROLE_: {}", nome);
            }
        }

        log.error("Nessun ruolo valido trovato tra le authority");
        throw new SecurityException("Ruolo non valido");
    }

    /* ESEMPIO JSON
    {
        "nomeOperazione": "aggiornaStatoVoto",
        "parametri": [12, true]
    }
     */
    @PostMapping("/operazione")
    public ResponseEntity<?> esegui(@RequestBody RichiestaOperazione richiesta) {
        TipoUtente ruolo = getRuoloDaContesto();
        utenteProxy.setRuolo(ruolo);

        // Log dettagliato dei parametri
        try {
            String parametriJson = new ObjectMapper().writeValueAsString(richiesta.getParametri());
            log.info("Chiamo eseguiOperazione con nomeOperazione='{}', parametri={}, ruolo={}",
                    richiesta.getNomeOperazione(), parametriJson, ruolo);
        } catch (Exception e) {
            log.warn("Impossibile serializzare i parametri in JSON: {}", e.getMessage());
        }

        try {
            Object risultato = utenteProxy.eseguiOperazione(
                    richiesta.getNomeOperazione(),
                    richiesta.getParametri()
            );

            log.info("Operazione '{}' completata con risultato: {}", richiesta.getNomeOperazione(), risultato);
            return ResponseEntity.ok(risultato);

        } catch (Exception e) {
            log.error("Errore durante l'esecuzione dell'operazione '{}': {}", richiesta.getNomeOperazione(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante l'esecuzione dell'operazione: " + e.getMessage());
        }
    }

}
