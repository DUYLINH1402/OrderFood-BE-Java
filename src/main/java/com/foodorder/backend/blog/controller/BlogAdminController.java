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
import com.foodorder.backend.security.annotation.RequireAdmin;
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
 * Controller quản lý bài viết/tin tức - API Admin
 * Yêu cầu quyền ADMIN để truy cập
 */
@RestController
@RequestMapping("/api/v1/admin/blogs")

@RequiredArgsConstructor
@RequireAdmin
@Tag(name = "Blogs - Admin", description = "Admin APIs for blog post management")
public class BlogAdminController {

    private final BlogService blogService;
    private final BlogCategoryService blogCategoryService;

    // ==================== BLOG APIs ====================

    @Operation(summary = "Get blog posts (Admin)",
            description = "Retrieve blog posts with filters and pagination")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<Page<BlogListResponse>> getBlogs(
            @Parameter(description = "Blog post title")
            @RequestParam(required = false) String title,
            @Parameter(description = "Blog post status")
            @RequestParam(required = false) BlogStatus status,
            @Parameter(description = "Content type (NEWS_PROMOTIONS, MEDIA_PRESS, CATERING_SERVICES)")
            @RequestParam(required = false) BlogType blogType,
            @Parameter(description = "Category ID")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Author ID")
            @RequestParam(required = false) Long authorId,
            @Parameter(description = "Pagination info")
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

    @Operation(summary = "Get blog post by ID (Admin)",
            description = "Retrieve full blog post content including drafts")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/{id}")
    public ResponseEntity<BlogResponse> getBlogById(
            @Parameter(description = "Blog post ID", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(blogService.getBlogById(id));
    }

    @Operation(summary = "Create blog post",
            description = "Create a new blog post with DRAFT or PUBLISHED status")
    @ApiResponse(responseCode = "201", description = "Created successfully")
    @PostMapping
    public ResponseEntity<BlogResponse> createBlog(
            @Parameter(description = "Blog post details", required = true)
            @Valid @RequestBody BlogRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        BlogResponse response = blogService.createBlog(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update blog post",
            description = "Update blog post content by ID")
    @ApiResponse(responseCode = "200", description = "Updated successfully")
    @PutMapping("/{id}")
    public ResponseEntity<BlogResponse> updateBlog(
            @Parameter(description = "Blog post ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated details", required = true)
            @Valid @RequestBody BlogRequest request) {
        return ResponseEntity.ok(blogService.updateBlog(id, request));
    }

    @Operation(summary = "Delete blog post",
            description = "Permanently delete a blog post by ID")
    @ApiResponse(responseCode = "204", description = "Deleted successfully")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(
            @Parameter(description = "Blog post ID", required = true)
            @PathVariable Long id) {
        blogService.deleteBlog(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update blog post status",
            description = "Change status: DRAFT, PUBLISHED, ARCHIVED")
    @ApiResponse(responseCode = "200", description = "Updated successfully")
    @PatchMapping("/{id}/status")
    public ResponseEntity<BlogResponse> updateBlogStatus(
            @Parameter(description = "Blog post ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "New status: DRAFT, PUBLISHED, ARCHIVED", required = true)
            @RequestParam String status) {
        return ResponseEntity.ok(blogService.updateBlogStatus(id, status));
    }

    // ==================== CATEGORY APIs ====================

    @Operation(summary = "Get all categories (Admin)",
            description = "Retrieve all categories including inactive ones")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/categories")
    public ResponseEntity<List<BlogCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(blogCategoryService.getAllCategories());
    }

    @Operation(summary = "Get all categories by content type",
            description = "Retrieve categories by type: NEWS_PROMOTIONS, MEDIA_PRESS, CATERING_SERVICES")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/categories/type/{blogType}")
    public ResponseEntity<List<BlogCategoryResponse>> getAllCategoriesByType(
            @Parameter(description = "Content type (NEWS_PROMOTIONS, MEDIA_PRESS, CATERING_SERVICES)", required = true)
            @PathVariable BlogType blogType) {
        return ResponseEntity.ok(blogCategoryService.getAllCategoriesByType(blogType));
    }

    @Operation(summary = "Get category by ID",
            description = "Retrieve category details including post count")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/categories/{id}")
    public ResponseEntity<BlogCategoryResponse> getCategoryById(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(blogCategoryService.getCategoryById(id));
    }

    @Operation(summary = "Create category",
            description = "Create a new blog category")
    @ApiResponse(responseCode = "201", description = "Created successfully")
    @PostMapping("/categories")
    public ResponseEntity<BlogCategoryResponse> createCategory(
            @Parameter(description = "Category details", required = true)
            @Valid @RequestBody BlogCategoryRequest request) {
        BlogCategoryResponse response = blogCategoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update category",
            description = "Update category details by ID")
    @ApiResponse(responseCode = "200", description = "Updated successfully")
    @PutMapping("/categories/{id}")
    public ResponseEntity<BlogCategoryResponse> updateCategory(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated details", required = true)
            @Valid @RequestBody BlogCategoryRequest request) {
        return ResponseEntity.ok(blogCategoryService.updateCategory(id, request));
    }

    @Operation(summary = "Delete category",
            description = "Delete a category (only when no posts belong to it)")
    @ApiResponse(responseCode = "204", description = "Deleted successfully")
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long id) {
        blogCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}

