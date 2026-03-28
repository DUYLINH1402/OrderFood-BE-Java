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
@Tag(name = "Search - Super Admin", description = "Algolia Search index management API - For Super Admin")
public class SuperAdminSearchController {

    private final AlgoliaSearchService algoliaSearchService;

    @PostMapping("/reindex")
    @Operation(
            summary = "Reindex all food items",
            description = "Sync all food data from MySQL to Algolia. " +
                    "Only SUPER_ADMIN can perform this action."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reindex started successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN required")
    })
    public ResponseEntity<String> reindexAll() {
        log.info("SUPER_ADMIN started reindexing all food items to Algolia");
        algoliaSearchService.reindexAll();
        return ResponseEntity.ok("Reindex started for all food items. Please check the logs to monitor progress.");
    }

    @PostMapping("/init")
    @Operation(
            summary = "Initialize Algolia data",
            description = "Push all food data from MySQL to Algolia for the first time. " +
                    "This API will clear all existing Algolia data (if any) and re-push from scratch. " +
                    "Only SUPER_ADMIN can perform this action."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Initialized successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - SUPER_ADMIN required")
    })
    public ResponseEntity<Map<String, Object>> initAlgoliaData() {
        log.info("SUPER_ADMIN started initializing Algolia data");

        int syncedCount = algoliaSearchService.initializeAlgoliaIndex();

        Map<String, Object> response = Map.of(
                "success", true,
                "message", "Algolia data initialized successfully",
                "syncedFoods", syncedCount
        );

        log.info("Algolia initialization completed with {} food items", syncedCount);
        return ResponseEntity.ok(response);
    }
}

