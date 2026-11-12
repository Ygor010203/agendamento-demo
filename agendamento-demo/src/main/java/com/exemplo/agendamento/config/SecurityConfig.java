package com.exemplo.agendamento.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.LinkedHashMap;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filtros(HttpSecurity http) throws Exception {

        // Sem login: chamadas da API recebem 401; páginas (ex.: /admin.html) vão para a tela de login.
        LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> pontos = new LinkedHashMap<>();
        pontos.put(new AntPathRequestMatcher("/api/**"), new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
        DelegatingAuthenticationEntryPoint semLogin = new DelegatingAuthenticationEntryPoint(pontos);
        semLogin.setDefaultEntryPoint(new LoginUrlAuthenticationEntryPoint("/login.html"));

        http
            .authorizeHttpRequests(a -> a
                // Páginas e arquivos da área pública
                .requestMatchers(HttpMethod.GET, "/", "/index.html", "/login.html",
                        "/css/**", "/js/**", "/favicon.ico").permitAll()
                .requestMatchers("/error").permitAll()
                // API usada pela página do cliente
                .requestMatchers(HttpMethod.GET, "/api/publico/**", "/api/barbeiros/*/disponibilidade").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/agendamentos/publico").permitAll()
                // Todo o resto (admin.html e o restante da API) exige login
                .anyRequest().authenticated())
            .formLogin(f -> f
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .successHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_NO_CONTENT))
                .failureHandler((req, res, ex) -> res.setStatus(HttpServletResponse.SC_UNAUTHORIZED))
                .permitAll())
            .logout(l -> l
                .logoutUrl("/logout")
                .logoutSuccessHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_NO_CONTENT)))
            .exceptionHandling(e -> e.authenticationEntryPoint(semLogin))
            // Proteção CSRF: o JavaScript lê o cookie XSRF-TOKEN e envia no cabeçalho X-XSRF-TOKEN.
            // O agendamento público é anônimo (não usa sessão), então fica de fora.
            .csrf(c -> c
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                .ignoringRequestMatchers("/api/agendamentos/publico"))
            .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Um único usuário (o dono). A senha vem de ADMIN_PASSWORD e é guardada criptografada em memória. */
    @Bean
    UserDetailsService usuarios(@Value("${app.admin.usuario}") String usuario,
                                @Value("${app.admin.senha}") String senha,
                                PasswordEncoder encoder) {
        if (senha == null || senha.length() < 8) {
            throw new IllegalStateException(
                    "Defina a senha do dono em ADMIN_PASSWORD (mínimo de 8 caracteres). "
                    + "Copie o arquivo .env.exemplo para .env e preencha.");
        }
        return new InMemoryUserDetailsManager(
                User.withUsername(usuario).password(encoder.encode(senha)).roles("DONO").build());
    }
}
