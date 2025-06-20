package com.universita.segreteria.security;

import com.universita.segreteria.controller.UtenteProxyController;
import com.universita.segreteria.model.Utente;
import com.universita.segreteria.repository.UtenteRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UtenteRepository utenteRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = null;

        // 1) Provo a leggere token dall'header Authorization
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        // 2) Se token non trovato nell'header, provo a leggerlo dai cookie
        if (token == null && request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 3) Se token trovato, eseguo validazione ed autenticazione
        if (token != null) {
            try {
                String email = jwtUtil.extractUsername(token);
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Optional<Utente> utenteOpt = utenteRepo.findByEmail(email);
                    if (utenteOpt.isPresent()) {
                        Utente utente = utenteOpt.get();
                        boolean valid = jwtUtil.isTokenValid(token, utente);
                        if (valid) {
                            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(utente, null, List.of(new SimpleGrantedAuthority("ROLE_" + utente.getRuolo().name())));
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                        } else {
                            logger.info("Token non valido per l'utente: {}", email);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Errore durante la validazione del token JWT", e);
            }
        }
        filterChain.doFilter(request, response);
    }

}
