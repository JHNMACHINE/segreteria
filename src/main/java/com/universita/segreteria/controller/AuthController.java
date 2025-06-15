package com.universita.segreteria.controller;

import com.universita.segreteria.dto.AuthRequest;
import com.universita.segreteria.dto.AuthResponse;
import com.universita.segreteria.dto.RegisterRequest;
import com.universita.segreteria.factory.UtenteFactory;
import com.universita.segreteria.model.Docente;
import com.universita.segreteria.model.PianoDiStudi;
import com.universita.segreteria.model.Studente;
import com.universita.segreteria.model.Utente;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
    DocenteService docenteService;

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
        if (nuovo instanceof Studente) {
            nuovo = studenteService.initStudente(request);
        } else if (nuovo instanceof Docente){
            nuovo = docenteService.initDocente(request);
        }

        utenteRepo.save(nuovo);
        logger.info("User registered successfully!");

        String token = jwtUtil.generateToken(nuovo);

        logger.debug("Auth Token: {}", token);
        return ResponseEntity.ok(new AuthResponse(token));
    }



    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        Optional<Utente> utenteOpt = utenteRepo.findByEmail(request.email());

        if (utenteOpt.isEmpty() || !passwordEncoder.matches(request.password(), utenteOpt.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credentials not valid");
        }

        String token = jwtUtil.generateToken(utenteOpt.get());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("Authorization") String bearerToken) {
        if (!bearerToken.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Token non valido");
        }

        String oldToken = bearerToken.substring(7);
        String newToken = jwtUtil.refreshToken(oldToken);
        return ResponseEntity.ok(new AuthResponse(newToken));
    }
}
