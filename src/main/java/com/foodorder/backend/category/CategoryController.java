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
@Tag(name = "Categories - Public", description = "Public APIs for food categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Get root categories", description = "Retrieve root categories (no parent).")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/roots")
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }

    @Operation(summary = "Get child categories by ID", description = "Retrieve child categories of a parent category by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Parent category not found")
    })
    @GetMapping("/by-parent/{parentId}")
    public ResponseEntity<List<CategoryResponse>> getByParentId(
            @Parameter(description = "Parent category ID") @PathVariable Long parentId) {
        return ResponseEntity.ok(categoryService.getCategoriesByParentId(parentId));
    }

    @Operation(summary = "Get child categories by slug", description = "Retrieve child categories of a parent category by slug.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Parent category not found")
    })
    @GetMapping("/by-parent-slug/{slug}")
    public ResponseEntity<List<CategoryResponse>> getByParentSlug(
            @Parameter(description = "Parent category slug") @PathVariable String slug) {
        CategoryResponse parent = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(categoryService.getCategoriesByParentId(parent.getId()));
    }

    @Operation(summary = "Get all categories", description = "Retrieve all categories.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "Get category by ID", description = "Retrieve category details by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Operation(summary = "Get category by slug", description = "Retrieve category details by slug.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(
            @Parameter(description = "Category slug") @PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getCategoryBySlug(slug));
    }
}
