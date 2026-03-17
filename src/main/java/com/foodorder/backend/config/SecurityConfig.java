package com.foodorder.backend.config;

import com.foodorder.backend.security.JwtAccessDeniedHandler;
import com.foodorder.backend.security.JwtAuthenticationEntryPoint;
import com.foodorder.backend.security.JwtAuthenticationFilter;
import com.foodorder.backend.security.OAuth2LoginFailureHandler;
import com.foodorder.backend.security.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Cấu hình Spring Security
 * Đặc biệt cho phép WebSocket endpoints hoạt động và xử lý lỗi JWT toàn cục
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    /**
     * SecurityFilterChain riêng cho Swagger - KHÔNG áp dụng bất kỳ security nào
     * Order = 1 để được xử lý TRƯỚC filterChain chính
     */
    @Bean
    @Order(1)
    public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(
                        "/swagger-ui/**",       // Quan trọng: Phải có /**
                        "/swagger-ui.html",     // Swagger UI HTML page
                        "/v3/api-docs/**",      // Khớp với springdoc.api-docs.path
                        "/v3/api-docs",
                        "/swagger-resources/**",
                        "/webjars/**"
                )
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Cấu hình xử lý lỗi authentication và authorization
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // Xử lý khi chưa đăng nhập
                        .accessDeniedHandler(jwtAccessDeniedHandler) // Xử lý khi không có quyền
                )

                .authorizeHttpRequests(auth -> auth
                        // ===== WebSocket endpoints =====
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/app/**").permitAll()     // STOMP destination prefix
                        .requestMatchers("/topic/**").permitAll()   // Message broker topics
                        .requestMatchers("/queue/**").permitAll()
                        .requestMatchers("/ws/staff-orders/**").permitAll()

                        // ===== Static resources & error page =====
                        .requestMatchers("/static/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // ===== OAuth2 Login endpoints =====
                        .requestMatchers("/oauth2/authorization/**").permitAll()
                        .requestMatchers("/login/oauth2/code/**").permitAll()

                        // ===== CONVENTION: /api/v1/{role}/** =====
                        .requestMatchers("/api/v1/public/**").permitAll()          // Public: không cần đăng nhập
                        .requestMatchers("/api/v1/client/**").authenticated()      // Client: người dùng đã đăng nhập
                        .requestMatchers("/api/v1/staff/**").hasAnyRole("STAFF", "ADMIN") // Staff: nhân viên
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")      // Admin: quản trị viên

                        // Các request khác cần authentication
                        .anyRequest().authenticated()
                )
                // Cấu hình OAuth2 Login (Google Authorization Code Flow)
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorization") // URL để bắt đầu OAuth2 flow: /oauth2/authorization/google
                        )
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/login/oauth2/code/*") // URL callback từ Google
                        )
                        .successHandler(oAuth2LoginSuccessHandler) // Xử lý khi đăng nhập thành công
                        .failureHandler(oAuth2LoginFailureHandler) // Xử lý khi đăng nhập thất bại
                )
                // Thêm JWT filter vào chain
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Sử dụng allowedOriginPatterns thay vì setAllowedOriginPatterns cho consistency
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "https://oder-4c1f2.web.app",
                "http://localhost:5173",
                "https://dongxanhfoodorder.shop",
                "https://dongxanhfood.shop"
        ));

        // Cho phép tất cả HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Cho phép tất cả headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Expose headers để browser có thể đọc
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Total-Count",
                "X-Page-Number",
                "X-Page-Size"
        ));

        // Cho phép credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache preflight response
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
