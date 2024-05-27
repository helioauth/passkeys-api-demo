package com.helioauth.passkeys.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/js/**", "/css/**", "/img/**", "/favicon.ico").permitAll()
                        .requestMatchers("/create-credential", "/register-credential").permitAll()
                        .requestMatchers("/signin-credential-options", "/signin-validate-key").permitAll()
                        .requestMatchers("/error/**").permitAll()

                        .anyRequest().fullyAuthenticated()
                )
//                .addFilterBefore((servletRequest, servletResponse, filterChain) -> {
//                    filterChain.doFilter(servletRequest, servletResponse);
//                }, UsernamePasswordAuthenticationFilter.class)
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
                .password("newthing")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }
}
