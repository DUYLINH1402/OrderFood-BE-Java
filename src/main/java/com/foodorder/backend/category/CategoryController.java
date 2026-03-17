package com.foodorder.backend.category;

import com.foodorder.backend.category.dto.response.CategoryResponse;
import com.foodorder.backend.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý danh mục món ăn - API Public
 * Các API này không cần đăng nhập
 */
@RestController
@RequestMapping("/api/v1/public/categories")
@RequiredArgsConstructor
@Tag(name = "Categories - Public", description = "API công khai danh mục món ăn")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Lấy danh mục gốc", description = "Lấy danh sách các danh mục cha (không có parent).")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/roots")
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }

    @Operation(summary = "Lấy danh mục con theo ID", description = "Lấy danh sách danh mục con của một danh mục cha theo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy danh mục cha")
    })
    @GetMapping("/by-parent/{parentId}")
    public ResponseEntity<List<CategoryResponse>> getByParentId(
            @Parameter(description = "ID của danh mục cha") @PathVariable Long parentId) {
        return ResponseEntity.ok(categoryService.getCategoriesByParentId(parentId));
    }

    @Operation(summary = "Lấy danh mục con theo Slug", description = "Lấy danh sách danh mục con của một danh mục cha theo slug.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy danh mục cha")
    })
    @GetMapping("/by-parent-slug/{slug}")
    public ResponseEntity<List<CategoryResponse>> getByParentSlug(
            @Parameter(description = "Slug của danh mục cha") @PathVariable String slug) {
        CategoryResponse parent = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(categoryService.getCategoriesByParentId(parent.getId()));
    }

    @Operation(summary = "Lấy tất cả danh mục", description = "Lấy danh sách tất cả danh mục.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "Chi tiết danh mục (ID)", description = "Lấy thông tin chi tiết của một danh mục theo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy danh mục")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "ID của danh mục") @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Operation(summary = "Chi tiết danh mục (Slug)", description = "Lấy thông tin chi tiết của một danh mục theo slug.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy danh mục")
    })
    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(
            @Parameter(description = "Slug của danh mục") @PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getCategoryBySlug(slug));
    }
}
