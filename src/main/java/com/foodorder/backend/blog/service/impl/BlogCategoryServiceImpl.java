package com.foodorder.backend.blog.service.impl;

import com.foodorder.backend.blog.dto.request.BlogCategoryRequest;
import com.foodorder.backend.blog.dto.response.BlogCategoryResponse;
import com.foodorder.backend.blog.entity.BlogCategory;
import com.foodorder.backend.blog.entity.BlogType;
import com.foodorder.backend.blog.repository.BlogCategoryRepository;
import com.foodorder.backend.blog.repository.BlogRepository;
import com.foodorder.backend.blog.service.BlogCategoryService;
import com.foodorder.backend.exception.BadRequestException;
import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.security.service.ProtectedDataService;
import com.foodorder.backend.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.foodorder.backend.config.CacheConfig.BLOG_CATEGORIES_CACHE;

/**
 * Service implementation quản lý danh mục tin tức
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BlogCategoryServiceImpl implements BlogCategoryService {

    private final BlogCategoryRepository blogCategoryRepository;
    private final BlogRepository blogRepository;
    private final ProtectedDataService protectedDataService;


    // ==================== PUBLIC APIs ====================

    /**
     * Lấy danh sách danh mục đang hoạt động (có cache)
     */
    @Override
    @Cacheable(value = BLOG_CATEGORIES_CACHE, key = "'active'")
    public List<BlogCategoryResponse> getActiveCategories() {
        log.info("Fetching active blog categories");
        return blogCategoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách danh mục đang hoạt động theo loại nội dung (có cache)
     */
    @Override
    @Cacheable(value = BLOG_CATEGORIES_CACHE, key = "'active_type_' + #blogType")
    public List<BlogCategoryResponse> getActiveCategoriesByType(BlogType blogType) {
        log.info("Fetching active blog categories by type: {}", blogType);
        return blogCategoryRepository.findByIsActiveTrueAndBlogTypeOrderByDisplayOrderAsc(blogType)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết danh mục theo slug
     */
    @Override
    public BlogCategoryResponse getCategoryBySlug(String slug) {
        BlogCategory category = blogCategoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Blog category not found with slug: " + slug,
                        "BLOG_CATEGORY_NOT_FOUND"
                ));

        if (!Boolean.TRUE.equals(category.getIsActive())) {
            throw new ResourceNotFoundException(
                    "Blog category is not available",
                    "BLOG_CATEGORY_NOT_AVAILABLE"
            );
        }

        return toResponse(category);
    }

    // ==================== ADMIN APIs ====================

    /**
     * Lấy tất cả danh mục (Admin)
     */
    @Override
    @Cacheable(value = BLOG_CATEGORIES_CACHE, key = "'all'")
    public List<BlogCategoryResponse> getAllCategories() {
        log.info("Admin: Fetching all blog categories");
        return blogCategoryRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả danh mục theo loại nội dung (Admin)
     */
    @Override
    @Cacheable(value = BLOG_CATEGORIES_CACHE, key = "'all_type_' + #blogType")
    public List<BlogCategoryResponse> getAllCategoriesByType(BlogType blogType) {
        log.info("Admin: Fetching all blog categories by type: {}", blogType);
        return blogCategoryRepository.findByBlogTypeOrderByDisplayOrderAsc(blogType)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết danh mục theo ID (Admin)
     */
    @Override
    public BlogCategoryResponse getCategoryById(Long id) {
        BlogCategory category = findCategoryById(id);
        return toResponse(category);
    }

    /**
     * Tạo mới danh mục (Admin)
     */
    @Override
    @Transactional
    @CacheEvict(value = BLOG_CATEGORIES_CACHE, allEntries = true)
    public BlogCategoryResponse createCategory(BlogCategoryRequest request) {
        log.info("Admin: Creating new blog category: {}", request.getName());

        // Tạo slug từ tên nếu không có
        String slug = request.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = SlugUtils.toSlug(request.getName());
        }

        // Kiểm tra slug đã tồn tại chưa
        if (blogCategoryRepository.existsBySlug(slug)) {
            throw new BadRequestException(
                    "Slug already exists: " + slug,
                    "BLOG_CATEGORY_SLUG_EXISTS"
            );
        }

        BlogCategory category = BlogCategory.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .blogType(request.getBlogType() != null ? request.getBlogType() : BlogType.NEWS_PROMOTIONS)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        category = blogCategoryRepository.save(category);
        log.info("Blog category created: ID={}, slug={}", category.getId(), category.getSlug());

        return toResponse(category);
    }

    /**
     * Cập nhật danh mục (Admin)
     * Nếu danh mục được bảo vệ (isProtected = true), chỉ SUPER_ADMIN mới có quyền cập nhật
     */
    @Override
    @Transactional
    @CacheEvict(value = BLOG_CATEGORIES_CACHE, allEntries = true)
    public BlogCategoryResponse updateCategory(Long id, BlogCategoryRequest request) {
        log.info("Admin: Updating blog category ID={}", id);

        BlogCategory category = findCategoryById(id);

        // Kiểm tra quyền nếu dữ liệu được bảo vệ
        checkProtectedDataPermission(category.getIsProtected(), "update");

        // Kiểm tra slug mới
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            if (blogCategoryRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
                throw new BadRequestException(
                        "Slug already exists: " + request.getSlug(),
                        "BLOG_CATEGORY_SLUG_EXISTS"
                );
            }
            category.setSlug(request.getSlug());
        }

        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getBlogType() != null) {
            category.setBlogType(request.getBlogType());
        }
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        category = blogCategoryRepository.save(category);
        log.info("Blog category updated: ID={}", category.getId());

        return toResponse(category);
    }

    /**
     * Xóa danh mục (Admin)
     * Chỉ cho phép xóa khi không có bài viết nào thuộc danh mục
     * Nếu danh mục được bảo vệ (isProtected = true), chỉ SUPER_ADMIN mới có quyền xóa
     */
    @Override
    @Transactional
    @CacheEvict(value = BLOG_CATEGORIES_CACHE, allEntries = true)
    public void deleteCategory(Long id) {
        log.info("Admin: Deleting blog category ID={}", id);

        BlogCategory category = findCategoryById(id);

        // Kiểm tra quyền nếu dữ liệu được bảo vệ
        checkProtectedDataPermission(category.getIsProtected(), "delete");

        // Kiểm tra có bài viết nào thuộc danh mục không
        long blogCount = blogRepository.countByCategoryId(id);
        if (blogCount > 0) {
            throw new BadRequestException(
                    "Cannot delete category with " + blogCount + " blog post(s)",
                    "BLOG_CATEGORY_HAS_BLOGS"
            );
        }

        blogCategoryRepository.delete(category);
        log.info("Blog category deleted: ID={}", id);
    }

    // ==================== Helper Methods ====================

    /**
     * Kiểm tra quyền thao tác trên dữ liệu được bảo vệ
     * Delegate sang ProtectedDataService để tránh code trùng lặp
     */
    private void checkProtectedDataPermission(Boolean isProtected, String action) {
        protectedDataService.checkPermission(isProtected, action);
    }

    /**
     * Tìm danh mục theo ID, throw exception nếu không tìm thấy
     */
    private BlogCategory findCategoryById(Long id) {
        return blogCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Blog category not found with ID: " + id,
                        "BLOG_CATEGORY_NOT_FOUND"
                ));
    }

    /**
     * Convert entity sang DTO response
     */
    private BlogCategoryResponse toResponse(BlogCategory category) {
        Long blogCount = blogRepository.countByCategoryId(category.getId());

        return BlogCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .blogType(category.getBlogType())
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .isProtected(category.getIsProtected())
                .blogCount(blogCount)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}

