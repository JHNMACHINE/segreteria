package com.universita.segreteria.controller;

import com.universita.segreteria.dto.AuthRequest;
import com.universita.segreteria.dto.AuthResponse;
import com.universita.segreteria.dto.RegisterRequest;
import com.universita.segreteria.factory.UtenteFactory;
import com.universita.segreteria.model.Utente;
import com.universita.segreteria.repository.UtenteRepository;
import com.universita.segreteria.security.JwtUtil;
import lombok.RequiredArgsConstructor;
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

    @Autowired
    private UtenteRepository utenteRepo;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (utenteRepo.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email già registrata");
        }

        Utente nuovo;
        try {
            nuovo = UtenteFactory.creaUtente(request.ruolo());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Ruolo non valido");
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
