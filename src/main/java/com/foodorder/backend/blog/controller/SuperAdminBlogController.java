package com.foodorder.backend.blog.controller;

import com.foodorder.backend.blog.dto.request.BlogCategoryRequest;
import com.foodorder.backend.blog.dto.request.BlogFilterRequest;
import com.foodorder.backend.blog.dto.request.BlogRequest;
import com.foodorder.backend.blog.dto.response.BlogCategoryResponse;
import com.foodorder.backend.blog.dto.response.BlogListResponse;
import com.foodorder.backend.blog.dto.response.BlogResponse;
import com.foodorder.backend.blog.entity.BlogStatus;
import com.foodorder.backend.blog.entity.BlogType;
import com.foodorder.backend.blog.service.BlogCategoryService;
import com.foodorder.backend.blog.service.BlogService;
import com.foodorder.backend.security.CustomUserDetails;
import com.foodorder.backend.security.annotation.RequireSuperAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý bài viết/tin tức dành cho Super Admin
 * SUPER_ADMIN có toàn quyền trên tất cả bài viết, bao gồm dữ liệu được bảo vệ (isProtected = true)
 *
 * Prefix: /api/v1/superadmin/blogs
 */
@RestController
@RequestMapping("/api/v1/superadmin/blogs")
@RequiredArgsConstructor
@RequireSuperAdmin
@Tag(name = "Blogs - Super Admin", description = "API quản lý bài viết dành cho Super Admin - Bao gồm dữ liệu được bảo vệ")
public class SuperAdminBlogController {

    private final BlogService blogService;
    private final BlogCategoryService blogCategoryService;

    // ==================== BLOG APIs ====================

    @Operation(summary = "Lấy danh sách bài viết (Super Admin)",
            description = "Lấy danh sách tất cả bài viết, bao gồm cả dữ liệu được bảo vệ.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping
    public ResponseEntity<Page<BlogListResponse>> getBlogs(
            @Parameter(description = "Tiêu đề bài viết")
            @RequestParam(required = false) String title,
            @Parameter(description = "Trạng thái bài viết")
            @RequestParam(required = false) BlogStatus status,
            @Parameter(description = "Loại nội dung (NEWS_PROMOTIONS, MEDIA_PRESS, CATERING_SERVICES)")
            @RequestParam(required = false) BlogType blogType,
            @Parameter(description = "ID danh mục")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "ID tác giả")
            @RequestParam(required = false) Long authorId,
            @Parameter(description = "Thông tin phân trang")
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        BlogFilterRequest filterRequest = BlogFilterRequest.builder()
                .title(title)
                .status(status)
                .blogType(blogType)
                .categoryId(categoryId)
                .authorId(authorId)
                .build();

        return ResponseEntity.ok(blogService.getBlogsWithFilter(filterRequest, pageable));
    }

    @Operation(summary = "Lấy chi tiết bài viết (Super Admin)",
            description = "Lấy nội dung đầy đủ, bao gồm cả bài viết được bảo vệ.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/{id}")
    public ResponseEntity<BlogResponse> getBlogById(
            @Parameter(description = "ID của bài viết", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(blogService.getBlogById(id));
    }

    @Operation(summary = "Tạo bài viết mới (Super Admin)",
            description = "Tạo bài viết mới với trạng thái DRAFT hoặc PUBLISHED.")
    @ApiResponse(responseCode = "201", description = "Tạo thành công")
    @PostMapping
    public ResponseEntity<BlogResponse> createBlog(
            @Valid @RequestBody BlogRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        BlogResponse response = blogService.createBlog(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Cập nhật bài viết (Super Admin)",
            description = "Cập nhật nội dung bài viết, bao gồm cả bài viết được bảo vệ.")
    @ApiResponse(responseCode = "200", description = "Cập nhật thành công")
    @PutMapping("/{id}")
    public ResponseEntity<BlogResponse> updateBlog(
            @Parameter(description = "ID của bài viết", required = true) @PathVariable Long id,
            @Valid @RequestBody BlogRequest request) {
        return ResponseEntity.ok(blogService.updateBlog(id, request));
    }

    @Operation(summary = "Xóa bài viết (Super Admin)",
            description = "Xóa vĩnh viễn bài viết, bao gồm cả bài viết được bảo vệ.")
    @ApiResponse(responseCode = "204", description = "Xóa thành công")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(
            @Parameter(description = "ID của bài viết", required = true) @PathVariable Long id) {
        blogService.deleteBlog(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cập nhật trạng thái bài viết (Super Admin)",
            description = "Thay đổi trạng thái: DRAFT, PUBLISHED, ARCHIVED - bao gồm cả bài viết được bảo vệ.")
    @ApiResponse(responseCode = "200", description = "Cập nhật thành công")
    @PatchMapping("/{id}/status")
    public ResponseEntity<BlogResponse> updateBlogStatus(
            @Parameter(description = "ID của bài viết", required = true) @PathVariable Long id,
            @Parameter(description = "Trạng thái mới: DRAFT, PUBLISHED, ARCHIVED", required = true)
            @RequestParam String status) {
        return ResponseEntity.ok(blogService.updateBlogStatus(id, status));
    }

    // ==================== CATEGORY APIs ====================

    @Operation(summary = "Lấy tất cả danh mục blog (Super Admin)",
            description = "Lấy danh sách tất cả danh mục bao gồm cả không hoạt động và được bảo vệ.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/categories")
    public ResponseEntity<List<BlogCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(blogCategoryService.getAllCategories());
    }

    @Operation(summary = "Chi tiết danh mục blog (Super Admin)",
            description = "Lấy thông tin danh mục bao gồm cả danh mục được bảo vệ.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/categories/{id}")
    public ResponseEntity<BlogCategoryResponse> getCategoryById(
            @Parameter(description = "ID của danh mục", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(blogCategoryService.getCategoryById(id));
    }

    @Operation(summary = "Tạo danh mục blog mới (Super Admin)",
            description = "Tạo danh mục tin tức mới.")
    @ApiResponse(responseCode = "201", description = "Tạo thành công")
    @PostMapping("/categories")
    public ResponseEntity<BlogCategoryResponse> createCategory(
            @Valid @RequestBody BlogCategoryRequest request) {
        BlogCategoryResponse response = blogCategoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Cập nhật danh mục blog (Super Admin)",
            description = "Cập nhật thông tin danh mục, bao gồm cả danh mục được bảo vệ.")
    @ApiResponse(responseCode = "200", description = "Cập nhật thành công")
    @PutMapping("/categories/{id}")
    public ResponseEntity<BlogCategoryResponse> updateCategory(
            @Parameter(description = "ID của danh mục", required = true) @PathVariable Long id,
            @Valid @RequestBody BlogCategoryRequest request) {
        return ResponseEntity.ok(blogCategoryService.updateCategory(id, request));
    }

    @Operation(summary = "Xóa danh mục blog (Super Admin)",
            description = "Xóa danh mục, bao gồm cả danh mục được bảo vệ.")
    @ApiResponse(responseCode = "204", description = "Xóa thành công")
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID của danh mục", required = true) @PathVariable Long id) {
        blogCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}

