package tn.esprit.spring.smartcity.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configure(http))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Auth — public
                .requestMatchers("/auth/login", "/auth/register").permitAll()
                .requestMatchers("/auth/change-password").authenticated()

                // Departments — lecture tous, écriture admin
                .requestMatchers(HttpMethod.GET, "/departments/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/departments/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/departments/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/departments/**").hasAuthority("ROLE_ADMIN")

                // Admin — admin only
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")

                // Agent creation — admin + manager
                .requestMatchers("/agent/create").hasAnyAuthority("ROLE_ADMIN", "ROLE_DEPARTMENT_MANAGER")

                // Agent — agent + admin/municipality
                .requestMatchers("/agent/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MUNICIPALITY", "ROLE_MUNICIPAL_AGENT")

                // Department — manager + admin/municipality
                .requestMatchers("/department/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MUNICIPALITY", "ROLE_DEPARTMENT_MANAGER")

                // Reports CRUD — any authenticated user
                .requestMatchers("/reports/**").authenticated()
                // Citizens
                .requestMatchers("/citizens/**").authenticated()
                // Notifications
                .requestMatchers("/notifications/**").authenticated()
                // AI/Chatbot
                .requestMatchers("/ai/**").authenticated()
                // File upload — public (browser <img> tags can't send JWT)
                .requestMatchers("/upload/**").permitAll()
                // Map
                .requestMatchers("/map/**").authenticated()
                // Gamification — any authenticated
                .requestMatchers("/gamification/**").authenticated()
                // Planning — agent + manager + admin/municipality
                .requestMatchers("/planning/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MUNICIPALITY", "ROLE_MUNICIPAL_AGENT", "ROLE_DEPARTMENT_MANAGER")
                // SLA — admin + municipality
                .requestMatchers("/sla/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MUNICIPALITY")

                // Catch-all
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
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
