package com.foodorder.backend.blog.dto.request;

import com.foodorder.backend.blog.entity.BlogType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO tạo mới/cập nhật danh mục tin tức
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogCategoryRequest {

    @NotBlank(message = "CATEGORY_NAME_REQUIRED")
    @Size(max = 100, message = "CATEGORY_NAME_MAX_LENGTH_100")
    private String name;

    @Size(max = 150, message = "SLUG_MAX_LENGTH_150")
    private String slug;

    @Size(max = 500, message = "DESCRIPTION_MAX_LENGTH_500")
    private String description;

    // Loại nội dung mà danh mục này thuộc về (mặc định NEWS_PROMOTIONS nếu không truyền)
    private BlogType blogType;

    private Integer displayOrder;

    private Boolean isActive;
}

