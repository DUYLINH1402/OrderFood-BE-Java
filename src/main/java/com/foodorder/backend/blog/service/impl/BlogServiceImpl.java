package com.foodorder.backend.blog.service.impl;

import com.foodorder.backend.blog.dto.request.BlogFilterRequest;
import com.foodorder.backend.blog.dto.request.BlogRequest;
import com.foodorder.backend.blog.dto.response.BlogCategoryResponse;
import com.foodorder.backend.blog.dto.response.BlogListResponse;
import com.foodorder.backend.blog.dto.response.BlogResponse;
import com.foodorder.backend.blog.entity.Blog;
import com.foodorder.backend.blog.entity.BlogCategory;
import com.foodorder.backend.blog.entity.BlogStatus;
import com.foodorder.backend.blog.entity.BlogType;
import com.foodorder.backend.blog.repository.BlogCategoryRepository;
import com.foodorder.backend.blog.repository.BlogRepository;
import com.foodorder.backend.blog.service.BlogService;
import com.foodorder.backend.config.RestPage;
import com.foodorder.backend.exception.BadRequestException;
import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.security.service.ProtectedDataService;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.user.repository.UserRepository;
import com.foodorder.backend.util.SlugUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.foodorder.backend.config.CacheConfig.*;

/**
 * Service implementation quản lý bài viết/tin tức
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final BlogCategoryRepository blogCategoryRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final ProtectedDataService protectedDataService;


    // ==================== PUBLIC APIs ====================

    /**
     * Lấy danh sách bài viết công khai (đã xuất bản)
     */
    @Override
    @Cacheable(value = BLOGS_CACHE, key = "'published_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<BlogListResponse> getPublishedBlogs(Pageable pageable) {
        log.info("Fetching published blogs - page: {}", pageable.getPageNumber());
        Pageable pageableWithSort = addDefaultSort(pageable);
        Page<BlogListResponse> page = blogRepository.findPublishedBlogs(LocalDateTime.now(), pageableWithSort)
                .map(this::toListResponse);
        return new RestPage<>(page.getContent(), page.getPageable(), page.getTotalElements());
    }

    /**
     * Lấy danh sách bài viết công khai theo loại nội dung (BlogType)
     */
    @Override
    @Cacheable(value = BLOGS_BY_TYPE_CACHE, key = "'type_' + #blogType + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<BlogListResponse> getPublishedBlogsByType(BlogType blogType, Pageable pageable) {
        log.info("Fetching blogs by content type: {}", blogType);
        Pageable pageableWithSort = addDefaultSort(pageable);
        Page<BlogListResponse> page = blogRepository.findPublishedBlogsByType(blogType, LocalDateTime.now(), pageableWithSort)
                .map(this::toListResponse);
        return new RestPage<>(page.getContent(), page.getPageable(), page.getTotalElements());
    }

    /**
     * Lấy danh sách bài viết công khai theo danh mục
     */
    @Override
    @Cacheable(value = BLOGS_BY_CATEGORY_CACHE, key = "'category_' + #categoryId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<BlogListResponse> getPublishedBlogsByCategory(Long categoryId, Pageable pageable) {
        log.info("Fetching blogs by category ID: {}", categoryId);
        Pageable pageableWithSort = addDefaultSort(pageable);
        Page<BlogListResponse> page = blogRepository.findPublishedBlogsByCategory(categoryId, LocalDateTime.now(), pageableWithSort)
                .map(this::toListResponse);
        return new RestPage<>(page.getContent(), page.getPageable(), page.getTotalElements());
    }

    /**
     * Lấy danh sách bài viết công khai theo slug danh mục
     */
    @Override
    @Cacheable(value = BLOGS_BY_CATEGORY_CACHE, key = "'slug_' + #categorySlug + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<BlogListResponse> getPublishedBlogsByCategorySlug(String categorySlug, Pageable pageable) {
        log.info("Fetching blogs by category slug: {}", categorySlug);
        Pageable pageableWithSort = addDefaultSort(pageable);
        Page<BlogListResponse> page = blogRepository.findPublishedBlogsByCategorySlug(categorySlug, LocalDateTime.now(), pageableWithSort)
                .map(this::toListResponse);
        return new RestPage<>(page.getContent(), page.getPageable(), page.getTotalElements());
    }

    /**
     * Lấy danh sách bài viết nổi bật
     */
    @Override
    @Cacheable(value = FEATURED_BLOGS_CACHE, key = "'featured_' + #limit")
    public List<BlogListResponse> getFeaturedBlogs(int limit) {
        log.info("Fetching {} featured blogs", limit);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedAt"));
        return blogRepository.findFeaturedBlogs(LocalDateTime.now(), pageable)
                .stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách bài viết nổi bật theo loại nội dung
     */
    @Override
    @Cacheable(value = FEATURED_BLOGS_CACHE, key = "'featured_type_' + #blogType + '_' + #limit")
    public List<BlogListResponse> getFeaturedBlogsByType(BlogType blogType, int limit) {
        log.info("Fetching {} featured blogs of type {}", limit, blogType);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedAt"));
        return blogRepository.findFeaturedBlogsByType(blogType, LocalDateTime.now(), pageable)
                .stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm bài viết công khai theo từ khóa
     */
    @Override
    public Page<BlogListResponse> searchPublishedBlogs(String keyword, Pageable pageable) {
        log.info("Searching blogs with keyword: {}", keyword);
        Pageable pageableWithSort = addDefaultSort(pageable);
        return blogRepository.searchPublishedBlogs(keyword, LocalDateTime.now(), pageableWithSort)
                .map(this::toListResponse);
    }

    /**
     * Lấy chi tiết bài viết công khai theo slug
     * Tự động tăng lượt xem
     */
    @Override
    @Transactional
    public BlogResponse getPublishedBlogBySlug(String slug) {
        log.info("Fetching blog detail by slug: {}", slug);

        Blog blog = blogRepository.findPublishedBlogBySlug(slug, LocalDateTime.now())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Blog not found with slug: " + slug,
                        "BLOG_NOT_FOUND"
                ));

        // Tăng lượt xem
        blogRepository.incrementViewCount(blog.getId());
        blog.setViewCount(blog.getViewCount() + 1);

        return toResponse(blog);
    }

    /**
     * Lấy danh sách bài viết liên quan
     */
    @Override
    @Cacheable(value = RELATED_BLOGS_CACHE, key = "'related_' + #blogId + '_' + #limit")
    public List<BlogListResponse> getRelatedBlogs(Long blogId, int limit) {
        log.info("Fetching related blogs for blog ID: {}", blogId);

        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Blog not found",
                        "BLOG_NOT_FOUND"
                ));

        if (blog.getCategory() == null) {
            return List.of();
        }

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedAt"));
        return blogRepository.findRelatedBlogs(
                        blog.getCategory().getId(),
                        blogId,
                        LocalDateTime.now(),
                        pageable)
                .stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }

    // ==================== ADMIN APIs ====================

    /**
     * Lấy danh sách bài viết với bộ lọc (Admin)
     */
    @Override
    public Page<BlogListResponse> getBlogsWithFilter(BlogFilterRequest filterRequest, Pageable pageable) {
        log.info("Admin: Fetching blogs with filter");
        Pageable pageableWithSort = addDefaultSort(pageable);
        return blogRepository.findWithFilter(
                        filterRequest.getTitle(),
                        filterRequest.getStatus(),
                        filterRequest.getBlogType(),
                        filterRequest.getCategoryId(),
                        filterRequest.getAuthorId(),
                        pageableWithSort)
                .map(this::toListResponse);
    }

    /**
     * Lấy chi tiết bài viết theo ID (Admin)
     */
    @Override
    public BlogResponse getBlogById(Long id) {
        log.info("Admin: Fetching blog detail ID: {}", id);
        Blog blog = findBlogById(id);
        return toResponse(blog);
    }

    /**
     * Tạo mới bài viết (Admin/Staff)
     */
    @Override
    @Transactional
    @CacheEvict(value = {BLOGS_CACHE, FEATURED_BLOGS_CACHE, BLOGS_BY_TYPE_CACHE, BLOGS_BY_CATEGORY_CACHE, RELATED_BLOGS_CACHE}, allEntries = true)
    public BlogResponse createBlog(BlogRequest request, Long authorId) {
        log.info("Admin: Creating new blog post: {}", request.getTitle());

        // Lấy thông tin tác giả
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found",
                        "USER_NOT_FOUND"
                ));

        // Tạo slug từ tiêu đề nếu không có
        String slug = request.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = SlugUtils.toSlug(request.getTitle());
        }

        // Kiểm tra slug đã tồn tại chưa
        if (blogRepository.existsBySlug(slug)) {
            // Thêm suffix để tạo slug unique
            int suffix = 1;
            String baseSlug = slug;
            while (blogRepository.existsBySlug(slug)) {
                slug = SlugUtils.toSlugWithSuffix(baseSlug, suffix++);
            }
        }

        // Lấy danh mục nếu có
        BlogCategory category = null;
        if (request.getCategoryId() != null) {
            category = blogCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Blog category not found",
                            "BLOG_CATEGORY_NOT_FOUND"
                    ));
        }

        // Xử lý thời điểm xuất bản
        LocalDateTime publishedAt = request.getPublishedAt();
        BlogStatus status = request.getStatus() != null ? request.getStatus() : BlogStatus.DRAFT;

        // Nếu status = PUBLISHED và không có publishedAt, set publishedAt = now
        if (status == BlogStatus.PUBLISHED && publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }

        // Xác định BlogType (mặc định NEWS_PROMOTIONS nếu không truyền)
        BlogType blogType = request.getBlogType() != null ? request.getBlogType() : BlogType.NEWS_PROMOTIONS;

        // Xử lý galleryImages từ List sang JSON string
        String galleryImagesJson = null;
        if (request.getGalleryImages() != null && !request.getGalleryImages().isEmpty()) {
            galleryImagesJson = convertListToJson(request.getGalleryImages());
        }

        Blog blog = Blog.builder()
                .title(request.getTitle())
                .slug(slug)
                .summary(request.getSummary())
                .content(request.getContent())
                .thumbnail(request.getThumbnail())
                .status(status)
                .blogType(blogType)
                .viewCount(0)
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .tags(request.getTags())
                // MEDIA_PRESS fields
                .sourceUrl(request.getSourceUrl())
                .sourceName(request.getSourceName())
                .sourceLogo(request.getSourceLogo())
                .sourcePublishedAt(request.getSourcePublishedAt())
                // CATERING_SERVICES fields
                .priceRange(request.getPriceRange())
                .serviceAreas(request.getServiceAreas())
                .menuItems(request.getMenuItems())
                .galleryImages(galleryImagesJson)
                .minCapacity(request.getMinCapacity())
                .maxCapacity(request.getMaxCapacity())
                .contactInfo(request.getContactInfo())
                // SEO fields
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .publishedAt(publishedAt)
                .author(author)
                .category(category)
                .build();

        blog = blogRepository.save(blog);
        log.info("Blog created: ID={}, slug={}", blog.getId(), blog.getSlug());

        return toResponse(blog);
    }

    /**
     * Cập nhật bài viết (Admin/Staff)
     * Nếu bài viết được bảo vệ (isProtected = true), chỉ SUPER_ADMIN mới có quyền cập nhật
     */
    @Override
    @Transactional
    @CacheEvict(value = {BLOGS_CACHE, FEATURED_BLOGS_CACHE, BLOGS_BY_TYPE_CACHE, BLOGS_BY_CATEGORY_CACHE, RELATED_BLOGS_CACHE}, allEntries = true)
    public BlogResponse updateBlog(Long id, BlogRequest request) {
        log.info("Admin: Updating blog ID: {}", id);

        Blog blog = findBlogById(id);

        // Kiểm tra quyền nếu dữ liệu được bảo vệ
        checkProtectedDataPermission(blog.getIsProtected(), "update");

        // Cập nhật slug nếu có
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            if (blogRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
                throw new BadRequestException(
                        "Slug already exists: " + request.getSlug(),
                        "BLOG_SLUG_EXISTS"
                );
            }
            blog.setSlug(request.getSlug());
        }

        // Cập nhật các trường khác
        if (request.getTitle() != null) {
            blog.setTitle(request.getTitle());
        }
        if (request.getSummary() != null) {
            blog.setSummary(request.getSummary());
        }
        if (request.getContent() != null) {
            blog.setContent(request.getContent());
        }
        if (request.getThumbnail() != null) {
            blog.setThumbnail(request.getThumbnail());
        }
        if (request.getIsFeatured() != null) {
            blog.setIsFeatured(request.getIsFeatured());
        }
        if (request.getTags() != null) {
            blog.setTags(request.getTags());
        }
        if (request.getMetaTitle() != null) {
            blog.setMetaTitle(request.getMetaTitle());
        }
        if (request.getMetaDescription() != null) {
            blog.setMetaDescription(request.getMetaDescription());
        }
        if (request.getPublishedAt() != null) {
            blog.setPublishedAt(request.getPublishedAt());
        }

        // Cập nhật BlogType nếu có
        if (request.getBlogType() != null) {
            blog.setBlogType(request.getBlogType());
        }

        // Cập nhật MEDIA_PRESS fields
        if (request.getSourceUrl() != null) {
            blog.setSourceUrl(request.getSourceUrl());
        }
        if (request.getSourceName() != null) {
            blog.setSourceName(request.getSourceName());
        }
        if (request.getSourceLogo() != null) {
            blog.setSourceLogo(request.getSourceLogo());
        }
        if (request.getSourcePublishedAt() != null) {
            blog.setSourcePublishedAt(request.getSourcePublishedAt());
        }

        // Cập nhật CATERING_SERVICES fields
        if (request.getPriceRange() != null) {
            blog.setPriceRange(request.getPriceRange());
        }
        if (request.getServiceAreas() != null) {
            blog.setServiceAreas(request.getServiceAreas());
        }
        if (request.getMenuItems() != null) {
            blog.setMenuItems(request.getMenuItems());
        }
        if (request.getGalleryImages() != null) {
            blog.setGalleryImages(convertListToJson(request.getGalleryImages()));
        }
        if (request.getMinCapacity() != null) {
            blog.setMinCapacity(request.getMinCapacity());
        }
        if (request.getMaxCapacity() != null) {
            blog.setMaxCapacity(request.getMaxCapacity());
        }
        if (request.getContactInfo() != null) {
            blog.setContactInfo(request.getContactInfo());
        }

        // Cập nhật status
        if (request.getStatus() != null) {
            BlogStatus oldStatus = blog.getStatus();
            blog.setStatus(request.getStatus());

            // Nếu chuyển sang PUBLISHED và chưa có publishedAt, set publishedAt = now
            if (request.getStatus() == BlogStatus.PUBLISHED
                    && oldStatus != BlogStatus.PUBLISHED
                    && blog.getPublishedAt() == null) {
                blog.setPublishedAt(LocalDateTime.now());
            }
        }

        // Cập nhật danh mục
        if (request.getCategoryId() != null) {
            BlogCategory category = blogCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Blog category not found",
                            "BLOG_CATEGORY_NOT_FOUND"
                    ));
            blog.setCategory(category);
        }

        blog = blogRepository.save(blog);
        log.info("Blog updated: ID={}", blog.getId());

        return toResponse(blog);
    }

    /**
     * Xóa bài viết (Admin)
     * Nếu bài viết được bảo vệ (isProtected = true), chỉ SUPER_ADMIN mới có quyền xóa
     */
    @Override
    @Transactional
    @CacheEvict(value = {BLOGS_CACHE, FEATURED_BLOGS_CACHE, BLOGS_BY_TYPE_CACHE, BLOGS_BY_CATEGORY_CACHE, RELATED_BLOGS_CACHE}, allEntries = true)
    public void deleteBlog(Long id) {
        log.info("Admin: Deleting blog ID: {}", id);

        Blog blog = findBlogById(id);

        // Kiểm tra quyền nếu dữ liệu được bảo vệ
        checkProtectedDataPermission(blog.getIsProtected(), "delete");

        blogRepository.delete(blog);

        log.info("Blog deleted: ID={}", id);
    }

    /**
     * Thay đổi trạng thái bài viết (Admin/Staff)
     * Nếu bài viết được bảo vệ (isProtected = true), chỉ SUPER_ADMIN mới có quyền cập nhật trạng thái
     */
    @Override
    @Transactional
    @CacheEvict(value = {BLOGS_CACHE, FEATURED_BLOGS_CACHE, BLOGS_BY_TYPE_CACHE, BLOGS_BY_CATEGORY_CACHE, RELATED_BLOGS_CACHE}, allEntries = true)
    public BlogResponse updateBlogStatus(Long id, String status) {
        log.info("Admin: Updating blog status ID: {} -> {}", id, status);

        Blog blog = findBlogById(id);

        // Kiểm tra quyền nếu dữ liệu được bảo vệ
        checkProtectedDataPermission(blog.getIsProtected(), "update status");

        BlogStatus newStatus;
        try {
            newStatus = BlogStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Invalid blog status: " + status,
                    "INVALID_BLOG_STATUS"
            );
        }

        BlogStatus oldStatus = blog.getStatus();
        blog.setStatus(newStatus);

        // Nếu chuyển sang PUBLISHED và chưa có publishedAt, set publishedAt = now
        if (newStatus == BlogStatus.PUBLISHED
                && oldStatus != BlogStatus.PUBLISHED
                && blog.getPublishedAt() == null) {
            blog.setPublishedAt(LocalDateTime.now());
        }

        blog = blogRepository.save(blog);
        log.info("Blog status updated: ID={}, status={}", id, newStatus);

        return toResponse(blog);
    }

    // ==================== Helper Methods ====================

    // Danh sách các field hợp lệ để sort
    private static final List<String> VALID_SORT_FIELDS = List.of(
            "id", "title", "slug", "status", "viewCount", "isFeatured",
            "publishedAt", "createdAt", "updatedAt"
    );

    /**
     * Thêm sort mặc định theo publishedAt DESC nếu Pageable không có sort hoặc sort không hợp lệ
     */
    private Pageable addDefaultSort(Pageable pageable) {
        // Nếu không có sort, dùng mặc định
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "publishedAt")
            );
        }

        // Kiểm tra xem các sort field có hợp lệ không
        boolean hasInvalidSort = pageable.getSort().stream()
                .anyMatch(order -> !VALID_SORT_FIELDS.contains(order.getProperty()));

        // Nếu có sort không hợp lệ, dùng sort mặc định
        if (hasInvalidSort) {
            log.warn("Invalid sort field, using default sort");
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "publishedAt")
            );
        }

        return pageable;
    }

    /**
     * Kiểm tra quyền thao tác trên dữ liệu được bảo vệ
     * Delegate sang ProtectedDataService để tránh code trùng lặp
     */
    private void checkProtectedDataPermission(Boolean isProtected, String action) {
        protectedDataService.checkPermission(isProtected, action);
    }

    /**
     * Tìm bài viết theo ID, throw exception nếu không tìm thấy
     */
    private Blog findBlogById(Long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Blog not found with ID: " + id,
                        "BLOG_NOT_FOUND"
                ));
    }

    /**
     * Convert entity sang DTO response (đầy đủ)
     */
    private BlogResponse toResponse(Blog blog) {
        BlogResponse.AuthorResponse authorResponse = null;
        if (blog.getAuthor() != null) {
            authorResponse = BlogResponse.AuthorResponse.builder()
                    .id(blog.getAuthor().getId())
                    .fullName(blog.getAuthor().getFullName())
                    .avatarUrl(blog.getAuthor().getAvatarUrl())
                    .build();
        }

        BlogCategoryResponse categoryResponse = null;
        if (blog.getCategory() != null) {
            categoryResponse = BlogCategoryResponse.builder()
                    .id(blog.getCategory().getId())
                    .name(blog.getCategory().getName())
                    .slug(blog.getCategory().getSlug())
                    .blogType(blog.getCategory().getBlogType())
                    .build();
        }

        return BlogResponse.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .slug(blog.getSlug())
                .summary(blog.getSummary())
                .content(blog.getContent())
                .thumbnail(blog.getThumbnail())
                .status(blog.getStatus())
                .blogType(blog.getBlogType())
                .viewCount(blog.getViewCount())
                .isFeatured(blog.getIsFeatured())
                .isProtected(blog.getIsProtected())
                .tags(blog.getTags())
                // MEDIA_PRESS fields
                .sourceUrl(blog.getSourceUrl())
                .sourceName(blog.getSourceName())
                .sourceLogo(blog.getSourceLogo())
                .sourcePublishedAt(blog.getSourcePublishedAt())
                // CATERING_SERVICES fields
                .priceRange(blog.getPriceRange())
                .serviceAreas(blog.getServiceAreas())
                .menuItems(blog.getMenuItems())
                .galleryImages(convertJsonToList(blog.getGalleryImages()))
                .minCapacity(blog.getMinCapacity())
                .maxCapacity(blog.getMaxCapacity())
                .contactInfo(blog.getContactInfo())
                // SEO fields
                .metaTitle(blog.getMetaTitle())
                .metaDescription(blog.getMetaDescription())
                .publishedAt(blog.getPublishedAt())
                .category(categoryResponse)
                .author(authorResponse)
                .createdAt(blog.getCreatedAt())
                .updatedAt(blog.getUpdatedAt())
                .build();
    }

    /**
     * Convert entity sang DTO response cho danh sách (rút gọn)
     */
    private BlogListResponse toListResponse(Blog blog) {
        BlogListResponse.AuthorInfo authorInfo = null;
        if (blog.getAuthor() != null) {
            authorInfo = BlogListResponse.AuthorInfo.builder()
                    .id(blog.getAuthor().getId())
                    .fullName(blog.getAuthor().getFullName())
                    .avatarUrl(blog.getAuthor().getAvatarUrl())
                    .build();
        }

        BlogListResponse.CategoryInfo categoryInfo = null;
        if (blog.getCategory() != null) {
            categoryInfo = BlogListResponse.CategoryInfo.builder()
                    .id(blog.getCategory().getId())
                    .name(blog.getCategory().getName())
                    .slug(blog.getCategory().getSlug())
                    .build();
        }

        return BlogListResponse.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .slug(blog.getSlug())
                .summary(blog.getSummary())
                .thumbnail(blog.getThumbnail())
                .status(blog.getStatus())
                .blogType(blog.getBlogType())
                .viewCount(blog.getViewCount())
                .isFeatured(blog.getIsFeatured())
                .isProtected(blog.getIsProtected())
                .tags(blog.getTags())
                // MEDIA_PRESS - chỉ hiển thị sourceName trong list
                .sourceName(blog.getSourceName())
                // CATERING_SERVICES - chỉ hiển thị priceRange trong list
                .priceRange(blog.getPriceRange())
                .publishedAt(blog.getPublishedAt())
                .category(categoryInfo)
                .author(authorInfo)
                .createdAt(blog.getCreatedAt())
                .build();
    }

    /**
     * Convert List<String> thành JSON string
     */
    private String convertListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("Error converting list to JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Convert JSON string thành List<String>
     */
    private List<String> convertJsonToList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to list: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}

