package com.foodorder.backend.blog.dto.request;

import com.foodorder.backend.blog.entity.BlogStatus;
import com.foodorder.backend.blog.entity.BlogType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO tạo mới/cập nhật bài viết
 * Hỗ trợ 3 loại nội dung:
 * - NEWS_PROMOTIONS: Tin tức, khuyến mãi
 * - MEDIA_PRESS: Báo chí nói về nhà hàng
 * - CATERING_SERVICES: Dịch vụ đãi tiệc
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogRequest {

    @NotBlank(message = "TITLE_REQUIRED")
    @Size(max = 255, message = "TITLE_MAX_LENGTH_255")
    private String title;

    @Size(max = 300, message = "SLUG_MAX_LENGTH_300")
    private String slug;

    @Size(max = 500, message = "SUMMARY_MAX_LENGTH_500")
    private String summary;

    private String content;

    @Size(max = 500, message = "THUMBNAIL_URL_MAX_LENGTH_500")
    private String thumbnail;

    private BlogStatus status;

    // Loại nội dung (mặc định NEWS_PROMOTIONS nếu không truyền)
    private BlogType blogType;

    private Boolean isFeatured;

    @Size(max = 500, message = "TAGS_MAX_LENGTH_500")
    private String tags;

    // ========== MEDIA_PRESS fields ==========
    @Size(max = 500, message = "SOURCE_URL_MAX_LENGTH_500")
    private String sourceUrl;

    @Size(max = 200, message = "SOURCE_NAME_MAX_LENGTH_200")
    private String sourceName;

    @Size(max = 500, message = "SOURCE_LOGO_MAX_LENGTH_500")
    private String sourceLogo;

    private LocalDateTime sourcePublishedAt;

    // ========== CATERING_SERVICES fields ==========
    @Size(max = 200, message = "PRICE_RANGE_MAX_LENGTH_200")
    private String priceRange;

    @Size(max = 1000, message = "SERVICE_AREAS_MAX_LENGTH_1000")
    private String serviceAreas;

    // Danh sách món ăn trong gói tiệc (JSON string hoặc list)
    private String menuItems;

    // Gallery hình ảnh thực tế (JSON array các URL hoặc list)
    private List<String> galleryImages;

    private Integer minCapacity;

    private Integer maxCapacity;

    @Size(max = 500, message = "CONTACT_INFO_MAX_LENGTH_500")
    private String contactInfo;

    // SEO fields
    @Size(max = 255, message = "META_TITLE_MAX_LENGTH_255")
    private String metaTitle;

    @Size(max = 500, message = "META_DESCRIPTION_MAX_LENGTH_500")
    private String metaDescription;

    // Thời điểm xuất bản (null = xuất bản ngay khi status = PUBLISHED)
    private LocalDateTime publishedAt;

    // ID danh mục
    private Long categoryId;
}

