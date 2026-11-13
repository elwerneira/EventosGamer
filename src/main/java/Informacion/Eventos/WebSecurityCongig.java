package Informacion.Eventos;

import jakarta.servlet.DispatcherType;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityCongig {
    private static final String CSP = "default-src 'self'; script-src 'none'; style-src 'self'; img-src 'self'; "
        + "font-src 'self'; connect-src 'self'; object-src 'none'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'";
    private final LoginRateLimitFilter loginLimit;

    public WebSecurityCongig(@Value("${security.login.max-attempts:30}") int maximum) {
        loginLimit = new LoginRateLimitFilter(maximum);
    }

    @Bean
    AuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher events) {
        return new DefaultAuthenticationEventPublisher(events);
    }

    private void securityHeaders(HttpSecurity http) throws Exception {
        http.headers(headers -> headers.contentSecurityPolicy(policy -> policy.policyDirectives(CSP))
            .referrerPolicy(policy -> policy.policy(ReferrerPolicy.NO_REFERRER))
            .addHeaderWriter(new StaticHeadersWriter("Cross-Origin-Resource-Policy", "same-origin"))
            .addHeaderWriter(new StaticHeadersWriter("Cross-Origin-Opener-Policy", "same-origin"))
            .addHeaderWriter(new StaticHeadersWriter("Cross-Origin-Embedder-Policy", "require-corp"))
            .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy", "camera=(), microphone=(), geolocation=()")));
    }
    @Bean
    @Order(1)
    SecurityFilterChain apiFilterChain(HttpSecurity http, JwtService jwt, AccountService accounts) throws Exception {
        securityHeaders(http);
        return http.securityMatcher("/api/**")
            // CSRF usa su propia cookie; la autenticación de la API sigue exigiendo Bearer.
            .csrf(csrf -> csrf.csrfTokenRepository(new CookieCsrfTokenRepository()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .requestCache(cache -> cache.disable())
            .authorizeHttpRequests(requests -> requests
                .requestMatchers(HttpMethod.GET, "/api/csrf").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/eventos").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, ex) -> JwtFilter.unauthorized(response))
                .accessDeniedHandler((request, response, ex) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"forbidden\"}");
                }))
            .addFilterBefore(new JwtFilter(jwt, accounts), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(loginLimit, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    @Order(2)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        securityHeaders(http);
        http.addFilterBefore(loginLimit, UsernamePasswordAuthenticationFilter.class);
        return http.authorizeHttpRequests(requests -> requests
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers("/", "/home", "/registro", "/login", "/login.css").permitAll()
                .requestMatchers(HttpMethod.GET, "/eventos").permitAll()
                .anyRequest().authenticated())
            .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/eventos").permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll())
            .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
