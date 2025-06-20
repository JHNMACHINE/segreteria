package com.universita.segreteria.controller;

import com.universita.segreteria.dto.AuthRequest;
import com.universita.segreteria.dto.AuthResponse;
import com.universita.segreteria.dto.RegisterRequest;
import com.universita.segreteria.factory.UtenteFactory;
import com.universita.segreteria.model.*;
import com.universita.segreteria.repository.UtenteRepository;
import com.universita.segreteria.security.JwtUtil;
import com.universita.segreteria.service.DocenteService;
import com.universita.segreteria.service.SegreteriaService;
import com.universita.segreteria.service.StudenteService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.simple.SimpleLoggerContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UtenteRepository utenteRepo;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private StudenteService studenteService;
    @Autowired
    private DocenteService docenteService;
    @Autowired
    private SegreteriaService segreteriaService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        logger.info(String.valueOf(request));
        if (utenteRepo.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email già registrata");
        }

        Utente nuovo;
        try {
            nuovo = UtenteFactory.creaUtente(request.ruolo());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Ruolo non valido");
        }

        // Aggiungi i nuovi campi
        switch (nuovo) {
            case Studente studente -> nuovo = studenteService.initStudente(request);
            case Docente docente -> nuovo = docenteService.initDocente(request);
            case Segretario segretario -> nuovo = segreteriaService.initSegretario(request);
            default -> {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid role");
            }
        }

        utenteRepo.save(nuovo);
        logger.info("User registered successfully!");

        String token = jwtUtil.generateToken(nuovo);

        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(false)         // NO HTTPS -> non abbiamo il cert
                .path("/")
                .maxAge(3600)         // 1 ora
                .sameSite("Strict")
                .build();

        logger.debug("Auth Token: {}", token);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Registrazione effettuata con successo"));
    }




    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        Optional<Utente> utenteOpt = utenteRepo.findByEmail(request.email());

        if (utenteOpt.isEmpty() || !passwordEncoder.matches(request.password(), utenteOpt.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credentials not valid");
        }

        Utente utente = utenteOpt.get();
        String token = jwtUtil.generateToken(utente);

        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(false)        // Metti true se hai HTTPS
                .path("/")
                .maxAge(3600)
                .sameSite("Strict")
                .build();

        Map<String, Object> responseBody = Map.of(
                "message", "Login effettuato con successo",
                "email", utente.getEmail(),
                "role", utente.getRuolo().name() // o getRuolo().toString()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(responseBody);
    }



    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "token", required = false) String oldToken) {
        if (oldToken == null || oldToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non presente");
        }

        String newToken;
        try {
            newToken = jwtUtil.refreshToken(oldToken);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido o scaduto");
        }

        ResponseCookie cookie = ResponseCookie.from("token", newToken)
                .httpOnly(true)
                .secure(false)      // NO HTTPS -> non abbiamo il cert
                .path("/")
                .maxAge(3600)      // 1 ora
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Token aggiornato con successo"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Crea un cookie "token" vuoto con maxAge=0 per cancellarlo dal browser
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(false)      // NO HTTPS -> non abbiamo il cert
                .path("/")
                .maxAge(0)         // cancella il cookie
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Logout effettuato con successo"));
    }


}
