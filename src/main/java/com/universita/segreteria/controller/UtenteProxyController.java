package com.universita.segreteria.controller;

import com.universita.segreteria.dto.RichiestaOperazione;
import com.universita.segreteria.model.TipoUtente;
import com.universita.segreteria.service.UserServiceProxy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UtenteProxyController {

    private final UserServiceProxy utenteProxy;

    /* ESEMPIO JSON
    {
        "nomeOperazione": "aggiornaStatoVoto",
        "parametri": [12, true]
    }
     */
    @PostMapping("/operazione")
    public ResponseEntity<?> esegui(@RequestBody RichiestaOperazione richiesta) {
        TipoUtente ruolo = richiesta.getRuolo();
        utenteProxy.setRuolo(ruolo);
        Object risultato = utenteProxy.eseguiOperazione(
                richiesta.getNomeOperazione(),
                richiesta.getParametri()
        );
        return ResponseEntity.ok(risultato);
    }
}
