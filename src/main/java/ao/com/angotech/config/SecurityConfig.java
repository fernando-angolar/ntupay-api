package ao.com.angotech.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CorsConfig corsConfig;

    public SecurityConfig(CorsConfig corsConfig) {
        this.corsConfig = corsConfig;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                // 1. Activar CORS com a config centralizada
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))

                // 2. CSRF desactivado (API stateless com JWT)
                .csrf(csrf -> csrf.disable())

                // 3. Sem sessão HTTP — stateless
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Autorização — todas as rotas públicas por agora
                //    TODO: restringir quando o filtro JWT estiver implementado
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());


        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
