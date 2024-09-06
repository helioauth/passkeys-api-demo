package com.helioauth.passkeys.demo.config;

import com.helioauth.passkeys.demo.client.DefaultPasskeysApiClient;
import com.helioauth.passkeys.demo.client.PasskeysApiClient;
import com.helioauth.passkeys.demo.service.PasskeyAuthenticationProvider;
import com.helioauth.passkeys.demo.service.UserSignInService;
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
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("Throwing3-Backspin-Divisive-Grouped")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasskeyAuthenticationProvider passkeyAuthenticationProvider(UserSignInService userSignInService) {
        return new PasskeyAuthenticationProvider(userSignInService);
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
    public WebAuthnAuthenticationProcessingFilter webAuthnFilter(AuthenticationManager authenticationManager) {
        WebAuthnAuthenticationProcessingFilter filter = new WebAuthnAuthenticationProcessingFilter();
        filter.setAuthenticationManager(authenticationManager);
        filter.setSessionAuthenticationStrategy(new ChangeSessionIdAuthenticationStrategy());
        filter.setSecurityContextRepository(new HttpSessionSecurityContextRepository());
        filter.setSecurityContextHolderStrategy(SecurityContextHolder.getContextHolderStrategy());

        return filter;
    }

    @Bean
    public PasskeysApiClient passkeysApiClient (@Value("${passkeys-api.uri}") String passkeysApiUri){
        return new DefaultPasskeysApiClient(passkeysApiUri);
    }
}
