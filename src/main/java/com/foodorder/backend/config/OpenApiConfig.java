
package com.foodorder.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Cấu hình OpenAPI (Swagger) cho API documentation
 * OpenAPI JSON: /v3/api-docs
 *
 * API được chia thành các nhóm theo Role:
 * - Public: Các API công khai không cần xác thực
 * - User: Các API dành cho người dùng đã đăng nhập
 * - Staff: Các API dành cho nhân viên
 * - Admin: Các API dành cho quản trị viên
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:Food Order Backend}")
    private String applicationName;

    /**
     * Cấu hình OpenAPI với thông tin API và JWT Security
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                // Thông tin API
                .info(new Info()
                        .title("Food Order API")
                        .description("RESTful API cho hệ thống đặt món ăn trực tuyến. " +
                                "Hỗ trợ các chức năng: xác thực người dùng, quản lý thực đơn, " +
                                "giỏ hàng, đơn hàng, thanh toán, điểm thưởng và nhiều hơn nữa.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Food Order Team")
                                .email("support@foodorder.com")
                                .url("https://dongxanhfood.shop"))
                        .license(new License()
                                .name("Private License")
                                .url("https://dongxanhfood.shop")))

                // Server endpoints
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://dongxanhfoodorder.shop")
                                .description("Production Server")))

                // Cấu hình JWT Bearer Authentication
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhập JWT token để xác thực. " +
                                                "Token được lấy từ API /api/v1/public/auth/login")));
    }

    /**
     * Nhóm API Public - Các API công khai không cần xác thực
     * Bao gồm: Auth, Foods, Categories, Search, Likes, Shares, Districts, Wards, Feedback, Payments
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("1. Public APIs")
                .displayName("Public")
                .pathsToMatch(
                        "/api/v1/public/**",
                        "/api/auth/**",
                        "/api/foods/**",
                        "/api/categories/**",
                        "/api/districts/**",
                        "/api/wards/**",
                        "/api/chatbot/**",
                        "/api/feedback-media/**",
                        "/api/likes/**",
                        "/api/shares/**",
                        "/api/payments/**"
                )
                .pathsToExclude(
                        "/api/admin/**",
                        "/api/staff/**"
                )
                .build();
    }

    /**
     * Nhóm API User - Các API dành cho người dùng đã đăng nhập
     * Bao gồm: Cart, Orders, Points, Coupons, Favorites, Notifications, Payments, Chat
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("2. User APIs")
                .displayName("User")
                .pathsToMatch(
                        "/api/v1/client/**",
                        "/api/cart/**",
                        "/api/orders/**",
                        "/api/points/**",
                        "/api/coupons/**",
                        "/api/favorites/**",
                        "/api/notifications/**",
                        "/api/notifications/user/**",
                        "/api/payments/**",
                        "/api/chat/**",
                        "/api/users/**"
                )
                .pathsToExclude(
                        "/api/admin/**",
                        "/api/staff/**",
                        "/api/v1/admin/**",
                        "/api/v1/staff/**",
                        "/api/notifications/staff/**"
                )
                .build();
    }

    /**
     * Nhóm API Staff - Các API dành cho nhân viên
     * Bao gồm: Dashboard, Quản lý đơn hàng, Notifications cho staff, Chat, Foods management
     */
    @Bean
    public GroupedOpenApi staffApi() {
        return GroupedOpenApi.builder()
                .group("3. Staff APIs")
                .displayName("Staff")
                .pathsToMatch(
                        "/api/v1/staff/**",
                        "/api/staff/**",
                        "/api/admin/dashboard/**",
                        "/api/notifications/staff/**",
                        "/api/chat/**",
                        "/api/staff/orders/**"
                )
                .build();
    }

    /**
     * Nhóm API Admin - Các API dành cho quản trị viên
     * Bao gồm: Quản lý Foods, Orders, Users, Employees, Dashboard, Coupons, Points Statistics
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("4. Admin APIs")
                .displayName("Admin")
                .pathsToMatch(
                        "/api/admin/**",
                        "/api/v1/admin/**"
                )
                .build();
    }

    /**
     * Nhóm tất cả API - Hiển thị toàn bộ endpoints
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("5. All APIs")
                .displayName("All APIs")
                .pathsToMatch("/api/**")
                .build();
    }
}
