package com.example.cashCard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

//    Базовая настройка защиты с помощью Spring Security
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // Проверяем каждый http запрос на предварительную аутентификацию
        http.authorizeHttpRequests(request -> request
                // проверяем все запросы которые начинаются с /cashcards
                .requestMatchers("/cashcards/**")
                        .hasRole("CARD-OWNER")) // проверяем роль нашего пользователя
                // настраиваем, что требуется базовая аутентификация по логину и паролю
                .httpBasic(Customizer.withDefaults())
                // настраиваем csrf(пока что отключили)
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    Настраиваем пользователя
    // Добавил пользователя только в память приложения для тестирования
    @Bean
    UserDetailsService testOnlyUsers(PasswordEncoder passwordEncoder) {
        User.UserBuilder users = User.builder();
        UserDetails hiss = users
                .username("hiss")
                .password(passwordEncoder.encode("abc123"))
                .roles("CARD-OWNER")
                .build();
        UserDetails arina = users
                .username("arina")
                .password(passwordEncoder.encode("qwer123"))
                .roles("CARD-OWNER")
                .build();
        UserDetails hankOwnsNoCards = users
                .username("hank-owns-no-cards")
                .password(passwordEncoder.encode("qwe123"))
                .roles("NON-OWNER")
                .build();
        return new InMemoryUserDetailsManager(hiss, hankOwnsNoCards, arina);
    }

}
