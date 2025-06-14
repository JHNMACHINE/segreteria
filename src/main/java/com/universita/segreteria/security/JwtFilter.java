package com.universita.segreteria.security;

import com.universita.segreteria.model.Utente;
import com.universita.segreteria.repository.UtenteRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UtenteRepository utenteRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            String email = jwtUtil.extractUsername(token);

            try {
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Optional<Utente> utenteOpt = utenteRepo.findByEmail(email);
                    if (utenteOpt.isPresent() && jwtUtil.isTokenValid(token, utenteOpt.get())) {
                        Utente utente = utenteOpt.get();

                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        utente, null, List.of(new SimpleGrantedAuthority("ROLE_" + utente.getRuolo()))
                                );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        // Log per errore di token
                        logger.warn("Token non valido per l'utente: " + email);
                    }
                }
            } catch (Exception e) {
                logger.error("Errore durante la validazione del token JWT", e);
            }
        }

        filterChain.doFilter(request, response);
    }

}
