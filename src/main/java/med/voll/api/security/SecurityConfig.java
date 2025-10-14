// med/voll/api/security/SecurityConfig.java
package med.voll.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true) // habilita @PreAuthorize
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwt) throws Exception {
        http
                // API stateless: sem sessão, sem CSRF
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Autorização por rotas
                .authorizeHttpRequests(auth -> auth
                        // Swagger liberado
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        // Endpoints públicos
                        .requestMatchers(HttpMethod.POST, "/medicos/cadastro", "/medicos/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/pacientes/cadastro", "/pacientes/login").permitAll()

                        // Protegidos (atenção ao C maiúsculo!)
                        .requestMatchers("/Consultas/**").authenticated()

                        // o restante: defina como quiser
                        .anyRequest().permitAll()
                )

                // Registra o filtro JWT antes do filtro de autenticação padrão
                .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
