package com.universita.segreteria.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter JwtFilter;

    public SecurityConfig(JwtFilter JwtFilter) {
        this.JwtFilter = JwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // API
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/operazione").authenticated()

                        // File statici pubblici
                        .requestMatchers(
                                "/js/**",
                                "/css/**",
                                "/images/**",
                                "/static/**",
                                "/", "/index.html", "/404.html",
                                "/segreteria/login.html", "/segreteria/register.html",
                                "/docente/login.html", "/docente/register.html",
                                "/studente/login.html", "/studente/register.html"
                        ).permitAll()

                        // Dashboard protette
                        .requestMatchers("/segreteria/**").authenticated()
                        .requestMatchers("/docente/**").authenticated()
                        .requestMatchers("/studente/**").authenticated()

                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendRedirect("/index.html")  // oppure un'altra pagina pubblica
                        )
                )
                .addFilterBefore(JwtFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(cors -> cors.configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues()))  // Aggiungi CORS
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
