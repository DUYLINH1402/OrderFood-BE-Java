package com.foodorder.backend.blog.controller;

import com.foodorder.backend.blog.dto.response.BlogCategoryResponse;
import com.foodorder.backend.blog.dto.response.BlogListResponse;
import com.foodorder.backend.blog.dto.response.BlogResponse;
import com.foodorder.backend.blog.entity.BlogType;
import com.foodorder.backend.blog.service.BlogCategoryService;
import com.foodorder.backend.blog.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Controller quản lý bài viết/tin tức - API Public
 * Các API này không cần đăng nhập
 */
@RestController
@RequestMapping("/api/v1/public/blogs")

@RequiredArgsConstructor
@Tag(name = "Blogs - Public", description = "Public APIs for blog posts and news")
public class BlogController {

    private final BlogService blogService;
    private final BlogCategoryService blogCategoryService;

    // ==================== BLOG APIs ====================

    @Operation(summary = "Get published blog posts",
            description = "Retrieve published blog posts with pagination support")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<Page<BlogListResponse>> getPublishedBlogs(
            @Parameter(description = "Pagination info")
            @PageableDefault(size = 10, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(blogService.getPublishedBlogs(pageable));
    }

    @Operation(summary = "Get featured blog posts",
            description = "Retrieve blog posts marked as featured")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/featured")
    public ResponseEntity<List<BlogListResponse>> getFeaturedBlogs(
            @Parameter(description = "Number of posts to retrieve")
            @RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(blogService.getFeaturedBlogs(limit));
    }

    // ==================== BLOG TYPE APIs ====================

    @Operation(summary = "Get blog posts by content type",
            description = "Retrieve blog posts by type: NEWS_PROMOTIONS, MEDIA_PRESS, CATERING_SERVICES")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/type/{blogType}")
    public ResponseEntity<Page<BlogListResponse>> getBlogsByType(
            @Parameter(description = "Content type (NEWS_PROMOTIONS, MEDIA_PRESS, CATERING_SERVICES)", required = true)
            @PathVariable BlogType blogType,
            @Parameter(description = "Pagination info")
            @PageableDefault(size = 10, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(blogService.getPublishedBlogsByType(blogType, pageable));
    }

    @Operation(summary = "Get featured blog posts by content type",
            description = "Retrieve featured blog posts by type: NEWS_PROMOTIONS, MEDIA_PRESS, CATERING_SERVICES")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/type/{blogType}/featured")
    public ResponseEntity<List<BlogListResponse>> getFeaturedBlogsByType(
            @Parameter(description = "Content type (NEWS_PROMOTIONS, MEDIA_PRESS, CATERING_SERVICES)", required = true)
            @PathVariable BlogType blogType,
            @Parameter(description = "Number of posts to retrieve")
            @RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(blogService.getFeaturedBlogsByType(blogType, limit));
    }

    @Operation(summary = "Search blog posts",
            description = "Search blog posts by keyword in title, summary and tags")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/search")
    public ResponseEntity<Page<BlogListResponse>> searchBlogs(
            @Parameter(description = "Search keyword", required = true)
            @RequestParam String keyword,
            @Parameter(description = "Pagination info")
            @PageableDefault(size = 10, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(blogService.searchPublishedBlogs(keyword, pageable));
    }

    @Operation(summary = "Get blog post by slug",
            description = "Retrieve full blog post content, automatically increments view count")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/{slug}")
    public ResponseEntity<BlogResponse> getBlogBySlug(
            @Parameter(description = "Blog post slug", required = true)
            @PathVariable String slug) {
        return ResponseEntity.ok(blogService.getPublishedBlogBySlug(slug));
    }

    @Operation(summary = "Get related blog posts",
            description = "Retrieve blog posts in the same category as the current post")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/{id}/related")
    public ResponseEntity<List<BlogListResponse>> getRelatedBlogs(
            @Parameter(description = "Current blog post ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Number of related posts to retrieve")
            @RequestParam(defaultValue = "4") int limit) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(blogService.getRelatedBlogs(id, limit));
    }

    // ==================== CATEGORY APIs ====================

    @Operation(summary = "Get active blog categories",
            description = "Retrieve active blog categories")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/categories")
    public ResponseEntity<List<BlogCategoryResponse>> getActiveCategories() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(blogCategoryService.getActiveCategories());
    }

    @Operation(summary = "Get blog categories by content type",
            description = "Retrieve active blog categories by type: NEWS_PROMOTIONS, MEDIA_PRESS, CATERING_SERVICES")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/categories/type/{blogType}")
    public ResponseEntity<List<BlogCategoryResponse>> getActiveCategoriesByType(
            @Parameter(description = "Content type (NEWS_PROMOTIONS, MEDIA_PRESS, CATERING_SERVICES)", required = true)
            @PathVariable BlogType blogType) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(blogCategoryService.getActiveCategoriesByType(blogType));
    }

    @Operation(summary = "Get category by slug",
            description = "Retrieve category details and post count")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/categories/{slug}")
    public ResponseEntity<BlogCategoryResponse> getCategoryBySlug(
            @Parameter(description = "Category slug", required = true)
            @PathVariable String slug) {
        return ResponseEntity.ok(blogCategoryService.getCategoryBySlug(slug));
    }

    @Operation(summary = "Get blog posts by category",
            description = "Retrieve blog posts belonging to a category by slug")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/categories/{slug}/posts")
    public ResponseEntity<Page<BlogListResponse>> getBlogsByCategory(
            @Parameter(description = "Category slug", required = true)
            @PathVariable String slug,
            @Parameter(description = "Pagination info")
            @PageableDefault(size = 10, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(blogService.getPublishedBlogsByCategorySlug(slug, pageable));
    }
}

