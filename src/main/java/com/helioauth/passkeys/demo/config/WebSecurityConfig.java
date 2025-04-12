package com.helioauth.passkeys.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helioauth.passkeys.demo.client.DefaultPasskeysApiClient;
import com.helioauth.passkeys.demo.client.PasskeysApiClient;
import com.helioauth.passkeys.demo.domain.UserRepository;
import com.helioauth.passkeys.demo.service.PasskeyAuthenticationProvider;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.session.SessionManagementFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   WebAuthnAuthenticationProcessingFilter webAuthnAuthenticationProcessingFilter,
                                                   AuthenticationManager authenticationManager) throws Exception {
        http
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/js/**", "/css/**", "/img/**", "/favicon.ico").permitAll()
                        .requestMatchers("/signin/**", "/signup/**").permitAll()
                        .requestMatchers("/error/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        .anyRequest().authenticated()
                )
                .authenticationManager(authenticationManager)
                .addFilterAfter(webAuthnAuthenticationProcessingFilter, SessionManagementFilter.class)
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
    @Deprecated
    public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationProvider passkeysAuthenticationProvider(UserRepository userRepository, PasskeysApiClient passkeysApiClient) {
        return new PasskeyAuthenticationProvider(userRepository, passkeysApiClient);
    }

    @Bean
    public AuthenticationManager authenticationManager(List<AuthenticationProvider> providers) {
        return new ProviderManager(providers);
    }

    @Bean
    public WebAuthnAuthenticationProcessingFilter webAuthnFilter(AuthenticationManager authenticationManager,
                                                                 ObjectMapper objectMapper) {
        WebAuthnAuthenticationProcessingFilter filter = new WebAuthnAuthenticationProcessingFilter();
        filter.setAuthenticationManager(authenticationManager);
        filter.setSessionAuthenticationStrategy(new ChangeSessionIdAuthenticationStrategy());
        filter.setSecurityContextRepository(new HttpSessionSecurityContextRepository());
        filter.setSecurityContextHolderStrategy(SecurityContextHolder.getContextHolderStrategy());
        filter.setObjectMapper(objectMapper);

        return filter;
    }

    @Bean
    public PasskeysApiClient passkeysApiClient (@Value("${passkeys-api.uri}") String passkeysApiUri,
                                                @Value("${passkeys-api.app-id}") String passkeysAppId,
                                                @Value("${passkeys-api.api-key}" ) String passkeysApiKey,
                                                ObjectMapper objectMapper) {
        return new DefaultPasskeysApiClient(passkeysApiUri, passkeysAppId, passkeysApiKey, objectMapper);
    }
}
