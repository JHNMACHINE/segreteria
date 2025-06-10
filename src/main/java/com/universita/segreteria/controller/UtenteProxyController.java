package com.universita.segreteria.controller;

import com.universita.segreteria.dto.RichiestaOperazione;
import com.universita.segreteria.model.TipoUtente;
import com.universita.segreteria.service.UserServiceProxy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UtenteProxyController {

    private final UserServiceProxy utenteProxy;

    // Simulazione ruolo utente (in un'app reale viene da token, sessione, ecc.)
    private TipoUtente getRuoloDaContesto() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().isEmpty()) {
            throw new SecurityException("Ruolo non disponibile");
        }

        String authority = auth.getAuthorities().iterator().next().getAuthority(); // es. ROLE_STUDENTE
        return TipoUtente.valueOf(authority.replace("ROLE_", ""));
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
        Object risultato = utenteProxy.eseguiOperazione(
                richiesta.getNomeOperazione(),
                richiesta.getParametri()
        );
        return ResponseEntity.ok(risultato);
    }
}
