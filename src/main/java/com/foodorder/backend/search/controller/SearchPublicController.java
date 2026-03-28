package com.foodorder.backend.search.controller;

import com.foodorder.backend.search.dto.FoodSearchDTO;
import com.foodorder.backend.search.dto.FoodSearchResponse;
import com.foodorder.backend.search.service.AlgoliaSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller public cho tìm kiếm món ăn qua Algolia
 * Không yêu cầu đăng nhập
 *
 * Đã migrate từ GET /api/v1/search → GET /api/v1/public/search (2026-03-17)
 */
@RestController
@RequestMapping("/api/v1/public/search")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Search - Public", description = "Food search API via Algolia - Public access")
public class SearchPublicController {

    private final AlgoliaSearchService algoliaSearchService;

    @GetMapping
    @Operation(
            summary = "Search for food",
            description = "Search for food items by keyword using Algolia full-text search. " +
                    "Supports searching by food name and description."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search successful"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<FoodSearchResponse> search(
            @Parameter(description = "Search keyword", required = true, example = "pho bo")
            @RequestParam String query,

            @Parameter(description = "Page number (starting from 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of results per page", example = "10")
            @RequestParam(defaultValue = "10") int hitsPerPage
    ) {
        log.info("Searching for food with query: '{}', page: {}, hitsPerPage: {}", query, page, hitsPerPage);

        List<FoodSearchDTO> results = algoliaSearchService.search(query, page, hitsPerPage);

        FoodSearchResponse response = FoodSearchResponse.builder()
                .results(results)
                .query(query)
                .totalResults(results.size())
                .page(page)
                .hitsPerPage(hitsPerPage)
                .build();

        return ResponseEntity.ok(response);
    }
}

