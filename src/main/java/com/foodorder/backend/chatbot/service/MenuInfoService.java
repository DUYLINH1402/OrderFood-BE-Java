package com.foodorder.backend.chatbot.service;

import com.foodorder.backend.category.entity.Category;
import com.foodorder.backend.category.repository.CategoryRepository;
import com.foodorder.backend.food.entity.Food;
import com.foodorder.backend.food.entity.FoodVariant;
import com.foodorder.backend.food.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service để lấy thông tin thực đơn từ database cho chatbot
 * Cung cấp dữ liệu chi tiết (tên, giá, biến thể) để chatbot trả lời chính xác
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MenuInfoService {

    private final FoodRepository foodRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Lấy thông tin tổng quan về thực đơn
     */
    public String getMenuOverview() {
        try {
            // Lấy tổng số món ăn
            long totalFoods = foodRepository.count();

            // Lấy danh sách category và số lượng món ăn trong mỗi category
            List<Category> categories = categoryRepository.findAll();

            StringBuilder menuInfo = new StringBuilder();
            menuInfo.append("🍽️ **THÔNG TIN THỰC ĐƠN** 🍽️\n\n");
            menuInfo.append("Chúng tôi hiện có **").append(totalFoods).append(" món ăn** đa dạng được phân loại theo:\n\n");

            // Thêm thông tin từng danh mục
            for (Category category : categories) {
                long foodCount = foodRepository.countByCategoryId(category.getId());
                if (foodCount > 0) {
                    menuInfo.append("🔸 **").append(category.getName()).append("**: ")
                            .append(foodCount).append(" món\n");
                }
            }

            // Thêm thông tin món nổi bật
            menuInfo.append("\n**MÓN NỔI BẬT:**\n");

            // Món bán chạy
            List<Food> bestSellers = foodRepository.findByIsBestSellerTrue(PageRequest.of(0, 5)).getContent();
            if (!bestSellers.isEmpty()) {
                menuInfo.append("🌟 **Món bán chạy**: ");
                menuInfo.append(bestSellers.stream()
                        .map(food -> food.getName() + " (" + formatPrice(food.getPrice()) + ")")
                        .collect(Collectors.joining(", ")));
                menuInfo.append("\n");
            }

            // Món mới
            List<Food> newFoods = foodRepository.findByIsNewTrue(PageRequest.of(0, 5)).getContent();
            if (!newFoods.isEmpty()) {
                menuInfo.append("🆕 **Món mới**: ");
                menuInfo.append(newFoods.stream()
                        .map(food -> food.getName() + " (" + formatPrice(food.getPrice()) + ")")
                        .collect(Collectors.joining(", ")));
                menuInfo.append("\n");
            }

            // Món đặc sắc
            List<Food> featuredFoods = foodRepository.findByIsFeaturedTrue(PageRequest.of(0, 5)).getContent();
            if (!featuredFoods.isEmpty()) {
                menuInfo.append("⭐ **Món đặc sắc**: ");
                menuInfo.append(featuredFoods.stream()
                        .map(food -> food.getName() + " (" + formatPrice(food.getPrice()) + ")")
                        .collect(Collectors.joining(", ")));
                menuInfo.append("\n");
            }

            menuInfo.append("\n💡 **Lưu ý**: Tất cả món ăn đều được chuẩn bị từ nguyên liệu tươi ngon, ");
            menuInfo.append("đảm bảo vệ sinh an toàn thực phẩm và có thể tùy chỉnh theo yêu cầu của quý khách!");

            return menuInfo.toString();

        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin thực đơn: {}", e.getMessage());
            return "Xin lỗi, hiện tại không thể lấy thông tin thực đơn. Vui lòng liên hệ hotline để được hỗ trợ!";
        }
    }

    /**
     * Lấy TOÀN BỘ danh sách món ăn kèm giá và biến thể — dùng làm context đầy đủ cho chatbot
     * Giúp chatbot có thể trả lời chính xác giá của bất kỳ món nào
     */
    public String getFullMenuWithPrices() {
        try {
            List<Food> allFoods = foodRepository.findAllActiveWithCategoryAndVariants();

            if (allFoods.isEmpty()) {
                return "Hiện tại chưa có món ăn nào trong thực đơn.";
            }

            // Nhóm theo danh mục
            Map<String, List<Food>> foodsByCategory = allFoods.stream()
                    .collect(Collectors.groupingBy(
                            food -> food.getCategory() != null ? food.getCategory().getName() : "Khác",
                            Collectors.toList()
                    ));

            StringBuilder menuInfo = new StringBuilder();
            menuInfo.append("DANH SÁCH ĐẦY ĐỦ CÁC MÓN ĂN VÀ GIÁ:\n\n");

            for (Map.Entry<String, List<Food>> entry : foodsByCategory.entrySet()) {
                menuInfo.append("📂 ").append(entry.getKey().toUpperCase()).append(":\n");

                for (Food food : entry.getValue()) {
                    menuInfo.append("- ").append(food.getName())
                            .append(" | Giá: ").append(formatPrice(food.getPrice()));

                    // Thêm thông tin biến thể (size, topping) nếu có
                    if (food.getVariants() != null && !food.getVariants().isEmpty()) {
                        menuInfo.append(" | Biến thể: ");
                        menuInfo.append(food.getVariants().stream()
                                .map(v -> v.getName() + " (+" + formatPrice(v.getExtraPrice()) + ")")
                                .collect(Collectors.joining(", ")));
                    }

                    // Nhãn đặc biệt
                    if (Boolean.TRUE.equals(food.getIsBestSeller())) menuInfo.append(" [Bán chạy]");
                    if (Boolean.TRUE.equals(food.getIsNew())) menuInfo.append(" [Mới]");
                    if (Boolean.TRUE.equals(food.getIsFeatured())) menuInfo.append(" [Đặc sắc]");

                    menuInfo.append("\n");
                }
                menuInfo.append("\n");
            }

            return menuInfo.toString();

        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách đầy đủ thực đơn: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Tìm kiếm và trả về thông tin chi tiết món ăn theo từ khóa (tên món)
     * Bao gồm: tên, giá, mô tả, biến thể, trạng thái.
     * Chiến lược tìm kiếm:
     * 1. Tìm chính xác theo cả cụm từ khóa
     * 2. Nếu không có, tách thành từng từ và tìm
     * 3. Nếu vẫn không có, tìm trong mô tả
     */
    public String searchFoodsByKeyword(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return "Vui lòng nhập từ khóa để tìm kiếm.";
            }

            String trimmedKeyword = keyword.trim();

            // Bước 1: Tìm kiếm theo cả cụm từ khóa
            List<Food> matchedFoods = new ArrayList<>(foodRepository.searchByNameForChatbot(trimmedKeyword));

            // Bước 2: Nếu không tìm thấy, tách thành từng từ và tìm kiếm
            if (matchedFoods.isEmpty() && trimmedKeyword.contains(" ")) {
                String[] words = trimmedKeyword.split("\\s+");
                Set<Long> addedIds = new HashSet<>();

                for (String word : words) {
                    if (word.length() >= 2) { // Chỉ tìm từ có ít nhất 2 ký tự
                        List<Food> partialMatches = foodRepository.searchByNameForChatbot(word);
                        for (Food food : partialMatches) {
                            if (addedIds.add(food.getId())) {
                                matchedFoods.add(food);
                            }
                        }
                    }
                }
            }

            // Bước 3: Nếu vẫn không tìm thấy, thử tìm trong mô tả
            if (matchedFoods.isEmpty()) {
                List<Food> allActive = foodRepository.findAllActiveWithCategoryAndVariants();
                matchedFoods = allActive.stream()
                        .filter(food -> food.getDescription() != null &&
                                food.getDescription().toLowerCase().contains(trimmedKeyword.toLowerCase()))
                        .limit(10)
                        .toList();
            }

            if (matchedFoods.isEmpty()) {
                return "Không tìm thấy món ăn nào phù hợp với từ khóa: \"" + keyword + "\"\n" +
                       "Bạn có thể thử tìm kiếm với các từ khóa khác hoặc xem thực đơn đầy đủ.";
            }

            StringBuilder result = new StringBuilder();
            result.append("KẾT QUẢ TÌM KIẾM CHO: \"").append(keyword).append("\"\n");
            result.append("Tìm thấy ").append(matchedFoods.size()).append(" món ăn phù hợp:\n\n");

            for (Food food : matchedFoods) {
                result.append("- ").append(food.getName()).append("\n");
                result.append("  Giá: ").append(formatPrice(food.getPrice())).append("\n");

                if (food.getDescription() != null && !food.getDescription().trim().isEmpty()) {
                    result.append("  Mô tả: ").append(food.getDescription()).append("\n");
                }

                if (food.getCategory() != null) {
                    result.append("  Danh mục: ").append(food.getCategory().getName()).append("\n");
                }

                // Thông tin biến thể chi tiết
                if (food.getVariants() != null && !food.getVariants().isEmpty()) {
                    result.append("  Các biến thể:\n");
                    for (FoodVariant variant : food.getVariants()) {
                        BigDecimal totalPrice = food.getPrice().add(
                                variant.getExtraPrice() != null ? variant.getExtraPrice() : BigDecimal.ZERO);
                        result.append("    + ").append(variant.getName())
                                .append(": phụ thu ").append(formatPrice(variant.getExtraPrice()))
                                .append(" → Tổng ").append(formatPrice(totalPrice));
                        if (Boolean.TRUE.equals(variant.getIsDefault())) {
                            result.append(" (mặc định)");
                        }
                        result.append("\n");
                    }
                }

                // Nhãn đặc biệt
                StringBuilder badges = new StringBuilder();
                if (Boolean.TRUE.equals(food.getIsBestSeller())) badges.append("Bán chạy ");
                if (Boolean.TRUE.equals(food.getIsNew())) badges.append("Mới ");
                if (Boolean.TRUE.equals(food.getIsFeatured())) badges.append("Đặc sắc ");
                if (!badges.isEmpty()) {
                    result.append("  Nhãn: ").append(badges.toString().trim()).append("\n");
                }
                result.append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm món ăn: {}", e.getMessage());
            return "Xin lỗi, có lỗi xảy ra khi tìm kiếm. Vui lòng thử lại sau!";
        }
    }

    /**
     * Lấy thông tin món ăn theo danh mục
     */
    public String getFoodsByCategory(String categoryName) {
        try {
            // Tìm category theo tên
            List<Category> categories = categoryRepository.findAll();
            Category matchedCategory = categories.stream()
                    .filter(cat -> cat.getName().toLowerCase().contains(categoryName.toLowerCase()))
                    .findFirst()
                    .orElse(null);

            if (matchedCategory == null) {
                return "❌ Không tìm thấy danh mục: **" + categoryName + "**\n\n" +
                       "Các danh mục hiện có: " + categories.stream()
                               .map(Category::getName)
                               .collect(Collectors.joining(", "));
            }

            List<Food> foods = foodRepository.findByCategoryId(matchedCategory.getId(), PageRequest.of(0, 20))
                    .getContent();

            if (foods.isEmpty()) {
                return "📂 Danh mục **" + matchedCategory.getName() + "** hiện chưa có món ăn nào.";
            }

            StringBuilder result = new StringBuilder();
            result.append("📂 **DANH MỤC: ").append(matchedCategory.getName().toUpperCase()).append("**\n\n");
            result.append("Có **").append(foods.size()).append(" món ăn** trong danh mục này:\n\n");

            for (Food food : foods) {
                result.append("🍽️ **").append(food.getName()).append("** - ")
                      .append(formatPrice(food.getPrice())).append("\n");

                if (food.getDescription() != null && !food.getDescription().trim().isEmpty()) {
                    result.append("   📝 ").append(food.getDescription()).append("\n");
                }
                result.append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("Lỗi khi lấy món ăn theo danh mục: {}", e.getMessage());
            return "Xin lỗi, có lỗi xảy ra khi lấy thông tin danh mục. Vui lòng thử lại sau!";
        }
    }

    /**
     * Format giá tiền
     */
    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "Liên hệ";
        }

        try {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return formatter.format(price).replace("₫", "VNĐ");
        } catch (Exception e) {
            log.warn("Lỗi khi format giá: {}", e.getMessage());
            return price.toString() + " VNĐ";
        }
    }
}
