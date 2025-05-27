package com.universita.segreteria.controller;

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

    @PostMapping("/inserisciStudente")
    public ResponseEntity<Studente> inserisciStudente(@RequestBody Studente studente) {
        utenteProxy.setRuolo(getRuoloDaContesto());
        Studente risultato = (Studente) utenteProxy.eseguiOperazione("inserisciStudente", studente);
        return ResponseEntity.ok(risultato);
    }

    @PostMapping("/accettaVoto/{votoId}")
    public ResponseEntity<Void> accettaVoto(@PathVariable Long votoId) {
        utenteProxy.setRuolo(getRuoloDaContesto());
        utenteProxy.eseguiOperazione("accettaVoto", votoId, true);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rifiutaVoto/{votoId}")
    public ResponseEntity<Void> rifiutaVoto(@PathVariable Long votoId) {
        utenteProxy.setRuolo(getRuoloDaContesto());
        utenteProxy.eseguiOperazione("accettaVoto", votoId, false);
        return ResponseEntity.ok().build();
    }

    // Altri endpoint proxy a seconda dei ruoli

}
