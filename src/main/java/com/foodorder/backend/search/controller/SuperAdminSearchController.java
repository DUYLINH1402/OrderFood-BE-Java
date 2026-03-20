package com.foodorder.backend.search.controller;

import com.foodorder.backend.search.service.AlgoliaSearchService;
import com.foodorder.backend.security.annotation.RequireSuperAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller quản lý Algolia Search index dành cho Super Admin
 * Yêu cầu quyền SUPER_ADMIN
 *
 * Prefix: /api/v1/superadmin/search
 */
@RestController
@RequestMapping("/api/v1/superadmin/search")
@RequiredArgsConstructor
@RequireSuperAdmin
@Slf4j
@Tag(name = "Search - Super Admin", description = "API quản lý Algolia Search index - Dành cho Super Admin")
public class SuperAdminSearchController {

    private final AlgoliaSearchService algoliaSearchService;

    @PostMapping("/reindex")
    @Operation(
            summary = "Reindex toàn bộ món ăn",
            description = "Đồng bộ lại toàn bộ dữ liệu món ăn từ MySQL lên Algolia. " +
                    "Chỉ SUPER_ADMIN mới có quyền thực hiện."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bắt đầu reindex thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập - Yêu cầu SUPER_ADMIN")
    })
    public ResponseEntity<String> reindexAll() {
        log.info("SUPER_ADMIN bắt đầu reindex toàn bộ món ăn lên Algolia");
        algoliaSearchService.reindexAll();
        return ResponseEntity.ok("Đã bắt đầu reindex toàn bộ món ăn. Vui lòng kiểm tra log để theo dõi tiến trình.");
    }

    @PostMapping("/init")
    @Operation(
            summary = "Khởi tạo dữ liệu Algolia",
            description = "Đẩy toàn bộ dữ liệu món ăn từ MySQL lên Algolia lần đầu tiên. " +
                    "API này sẽ xóa toàn bộ dữ liệu cũ trên Algolia (nếu có) và đẩy lại từ đầu. " +
                    "Chỉ SUPER_ADMIN mới có quyền thực hiện."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Khởi tạo thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập - Yêu cầu SUPER_ADMIN")
    })
    public ResponseEntity<Map<String, Object>> initAlgoliaData() {
        log.info("SUPER_ADMIN bắt đầu khởi tạo dữ liệu Algolia");

        int syncedCount = algoliaSearchService.initializeAlgoliaIndex();

        Map<String, Object> response = Map.of(
                "success", true,
                "message", "Khởi tạo dữ liệu Algolia thành công",
                "syncedFoods", syncedCount
        );

        log.info("Hoàn tất khởi tạo Algolia với {} món ăn", syncedCount);
        return ResponseEntity.ok(response);
    }
}

