package com.helioauth.passkeys.demo.config;

import com.helioauth.passkeys.demo.service.PasskeyAuthenticationProvider;
import com.helioauth.passkeys.demo.service.WebAuthnAuthenticator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.session.SessionManagementFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, PasskeyTestFilter passkeyTestFilter, AuthenticationManager authenticationManager) throws Exception {
        http
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/js/**", "/css/**", "/img/**", "/favicon.ico").permitAll()
                        .requestMatchers("/create-credential", "/register-credential").permitAll()
                        .requestMatchers("/signin-credential-options", "/signin-validate-key").permitAll()
                        .requestMatchers("/error/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        .anyRequest().authenticated()
                )
                .authenticationManager(authenticationManager)
                .addFilterAfter(passkeyTestFilter, SessionManagementFilter.class)
                .formLogin((form) -> form
                        .loginPage("/signin")
                        .defaultSuccessUrl("/")
                        .permitAll()
                )
                .logout(LogoutConfigurer::permitAll)
                .csrf(AbstractHttpConfigurer::disable)
        ;

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("Throwing3-Backspin-Divisive-Grouped")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasskeyAuthenticationProvider passkeyAuthenticationProvider(WebAuthnAuthenticator webAuthnAuthenticator) {
        return new PasskeyAuthenticationProvider(webAuthnAuthenticator);
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(List<AuthenticationProvider> providers) {
        return new ProviderManager(providers);
    }

    @Bean
    public PasskeyTestFilter passkeyTestFilter(AuthenticationManager authenticationManager) {
        PasskeyTestFilter passkeyTestFilter = new PasskeyTestFilter();
        passkeyTestFilter.setAuthenticationManager(authenticationManager);
        passkeyTestFilter.setSessionAuthenticationStrategy(new ChangeSessionIdAuthenticationStrategy());
        passkeyTestFilter.setSecurityContextRepository(new HttpSessionSecurityContextRepository());
        passkeyTestFilter.setSecurityContextHolderStrategy(SecurityContextHolder.getContextHolderStrategy());

        return passkeyTestFilter;
    }

}
