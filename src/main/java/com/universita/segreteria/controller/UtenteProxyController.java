package com.universita.segreteria.controller;

import com.universita.segreteria.dto.RichiestaOperazione;
import com.universita.segreteria.model.Studente;
import com.universita.segreteria.model.TipoUtente;
import com.universita.segreteria.service.UtenteServiceProxy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UtenteProxyController {

    private final UtenteServiceProxy utenteProxy;

    // Simulazione ruolo utente (in un'app reale viene da token, sessione, ecc.)
    private TipoUtente getRuoloDaContesto() {
        return TipoUtente.STUDENTE; // oppure DOCENTE, SEGRETARIO per test
    }



    /* ESEMPIO JSON
    {
        "nomeOperazione": "accettaVoto",
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
