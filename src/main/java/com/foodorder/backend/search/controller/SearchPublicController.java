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
@Tag(name = "Search - Public", description = "API tìm kiếm món ăn qua Algolia - Công khai")
public class SearchPublicController {

    private final AlgoliaSearchService algoliaSearchService;

    @GetMapping
    @Operation(
            summary = "Tìm kiếm món ăn",
            description = "Tìm kiếm món ăn theo từ khóa sử dụng Algolia full-text search. " +
                    "Hỗ trợ tìm kiếm theo tên và mô tả món ăn."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm kiếm thành công"),
            @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ")
    })
    public ResponseEntity<FoodSearchResponse> search(
            @Parameter(description = "Từ khóa tìm kiếm", required = true, example = "phở bò")
            @RequestParam String query,

            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Số kết quả mỗi trang", example = "10")
            @RequestParam(defaultValue = "10") int hitsPerPage
    ) {
        log.info("Tìm kiếm món ăn với query: '{}', page: {}, hitsPerPage: {}", query, page, hitsPerPage);

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

