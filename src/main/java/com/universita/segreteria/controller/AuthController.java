package com.universita.segreteria.controller;

import com.universita.segreteria.dto.AuthRequest;
import com.universita.segreteria.dto.AuthResponse;
import com.universita.segreteria.dto.RegisterRequest;
import com.universita.segreteria.model.*;
import com.universita.segreteria.model.Segretario;
import com.universita.segreteria.repository.UtenteRepository;
import com.universita.segreteria.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UtenteRepository utenteRepo;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (utenteRepo.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email già registrata");
        }

        Utente nuovo;
        switch (request.ruolo()) {
            case STUDENTE -> nuovo = new Studente();
            case DOCENTE -> nuovo = new Docente();
            case SEGRETARIO -> nuovo = new Segretario();
            default -> {
                return ResponseEntity.badRequest().body("Ruolo non valido");
            }
        }

        nuovo.setEmail(request.email());
        nuovo.setPassword(passwordEncoder.encode(request.password()));
        nuovo.setRuolo(request.ruolo());
        nuovo.setNome(request.nome());
        nuovo.setCognome(request.cognome());
        nuovo.setMatricola(request.matricola());

        utenteRepo.save(nuovo);

        String token = jwtUtil.generateToken(nuovo);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        Optional<Utente> utenteOpt = utenteRepo.findByEmail(request.email());

        if (utenteOpt.isEmpty() || !passwordEncoder.matches(request.password(), utenteOpt.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenziali non valide");
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
