package com.koreait.moviesite.DetailpageReserve.config;

import com.koreait.moviesite.Member.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/login", "/signup", "/mypage", "/admin", "/error",
                    "/Member/**", "/DetailpageReserve/**", "/RankingGenreboard/**", "/common/**",
                    "/uploads/**", "/favicon.ico"
                ).permitAll()
                .requestMatchers("/api/auth/**", "/api/home/**", "/api/boxoffice/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/movies", "/movies/**", "/ranking", "/genre", "/board/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/member/**", "/api/reservations/**", "/api/screenings/**").authenticated()
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf
                // Bearer 토큰 API는 브라우저 쿠키 인증에 의존하지 않으므로 CSRF 대상에서 제외한다.
                .ignoringRequestMatchers("/api/**")
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ★ 비밀번호 암호화용 Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
