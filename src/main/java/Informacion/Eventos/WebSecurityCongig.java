package Informacion.Eventos;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityCongig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(requests -> requests
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers("/", "/home", "/registro", "/login", "/login.css").permitAll()
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
