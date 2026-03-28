package com.foodorder.backend.chatbot.service;

import com.foodorder.backend.chatbot.entity.KnowledgeBase;
import com.foodorder.backend.chatbot.repository.KnowledgeBaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service xử lý hệ thống RAG (Retrieval-Augmented Generation)
 * Tìm kiếm và truy xuất thông tin từ knowledge base, Database để cung cấp context cho chatbot.
 *
 * Chiến lược: Luôn kèm full menu vào context để GPT có thể trả lời mọi câu hỏi về món ăn,
 * kể cả câu hỏi follow-up ngắn gọn như "Thế còn Khoai tây chiên".
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RAGService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final MenuInfoService menuInfoService;

    @Value("${chatbot.context.similarity-threshold:0.7}")
    private Double similarityThreshold;

    /**
     * Tìm kiếm context phù hợp từ knowledge base cho câu hỏi của user.
     * Chiến lược mới: LUÔN kèm full menu context để chatbot biết mọi món ăn + giá,
     * đồng thời bổ sung kết quả tìm kiếm cụ thể nếu nhận diện được tên món.
     */
    public String retrieveRelevantContext(String userMessage) {
        try {
            StringBuilder contextBuilder = new StringBuilder();

            // Bước 1: Thử trích xuất tên món ăn từ câu hỏi (nếu hỏi về món cụ thể)
            String foodName = extractFoodNameFromMessage(userMessage);
            if (foodName != null && !foodName.isEmpty()) {
                // Tìm kiếm cụ thể trong DB
                String searchResult = menuInfoService.searchFoodsByKeyword(foodName);
                contextBuilder.append("THÔNG TIN MÓN ĂN ĐƯỢC HỎI:\n\n").append(searchResult).append("\n\n");
            }

            // Bước 2: Kiểm tra nếu là câu hỏi liên quan đến món ăn/thực đơn/giá cả
            // → Kèm FULL MENU để chatbot có toàn bộ dữ liệu
            if (isFoodRelatedQuery(userMessage) || foodName != null) {
                String fullMenu = menuInfoService.getFullMenuWithPrices();
                contextBuilder.append(fullMenu).append("\n");
                contextBuilder.append("---\n");
                contextBuilder.append("QUAN TRỌNG: Hãy tìm trong DANH SÁCH ĐẦY ĐỦ ở trên để trả lời. ");
                contextBuilder.append("Nếu món ăn có biến thể (size, topping), thông báo giá gốc + phụ thu. ");
                contextBuilder.append("Nếu không tìm thấy chính xác tên món khách hỏi, hãy gợi ý các món có tên gần giống.\n");
                return contextBuilder.toString();
            }

            // Bước 3: Không liên quan đến món ăn → tìm trong knowledge base
            List<String> keywords = extractKeywords(userMessage);
            List<KnowledgeBase> relevantKnowledge = searchKnowledgeBase(keywords);

            if (!relevantKnowledge.isEmpty()) {
                contextBuilder.append(buildContextFromKnowledge(relevantKnowledge));
            }

            // Bước 4: FALLBACK — Nếu không tìm được gì từ knowledge base,
            // vẫn kèm full menu vì có thể là câu hỏi follow-up về món ăn
            // mà ta không nhận diện được (ví dụ: "Thế còn Khoai tây chiên")
            if (contextBuilder.isEmpty()) {
                String fullMenu = menuInfoService.getFullMenuWithPrices();
                contextBuilder.append(fullMenu).append("\n");
                contextBuilder.append("---\n");
                contextBuilder.append("Đây là toàn bộ thực đơn nhà hàng. Nếu khách hỏi về món ăn, hãy tìm trong danh sách trên. ");
                contextBuilder.append("Nếu câu hỏi không liên quan đến món ăn, hãy trả lời tự nhiên.\n");
            }

            return contextBuilder.toString();

        } catch (Exception e) {
            log.error("Error searching context: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Kiểm tra xem câu hỏi có liên quan đến món ăn, thực đơn, giá cả không.
     * Bao gồm: hỏi giá, hỏi có món X không, hỏi thực đơn, hỏi đặt món, câu hỏi follow-up...
     */
    private boolean isFoodRelatedQuery(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }

        String lowerMessage = message.toLowerCase();

        // Nhóm 1: Từ khóa liên quan đến giá cả
        String[] priceKeywords = {
            "giá", "bao nhiêu", "bao nhiu", "bnh", "bnhiu", "price",
            "tiền", "chi phí", "giá cả", "giá bao", "giá bán",
            "mắc không", "rẻ không", "đắt không", "cost", "how much"
        };

        // Nhóm 2: Từ khóa liên quan đến thực đơn tổng quan
        String[] menuKeywords = {
            "thực đơn", "menu", "món ăn", "đồ ăn", "food",
            "có món gì", "món nào", "ăn gì", "tìm món", "xem món",
            "bán gì", "phục vụ gì", "danh sách món",
            "có những món", "các món", "tất cả món"
        };

        // Nhóm 3: Từ khóa hỏi về món ăn cụ thể hoặc đặt hàng
        String[] specificFoodKeywords = {
            "có món", "có bán", "cho tôi", "cho xin", "cho em",
            "tôi muốn", "muốn ăn", "muốn gọi", "muốn đặt", "muốn order",
            "order", "nên ăn", "ngon không", "gợi ý", "tư vấn",
            "thông tin về", "cho biết về", "mô tả",
            "ở đây có", "nhà hàng có", "quán có", "shop có",
            "bên mình có", "bên em có", "bên bạn có"
        };

        // Nhóm 4: Từ khóa follow-up (câu hỏi tiếp nối, thường ngắn gọn)
        String[] followUpKeywords = {
            "thế còn", "còn có", "vậy còn", "entao", "thế",
            "à còn", "ngoài ra", "có thêm", "gì nữa", "gì khác"
        };

        // Kiểm tra tất cả nhóm
        String[][] allGroups = {priceKeywords, menuKeywords, specificFoodKeywords, followUpKeywords};
        for (String[] group : allGroups) {
            for (String keyword : group) {
                if (lowerMessage.contains(keyword)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Trích xuất tên món ăn từ câu hỏi của khách hàng.
     * Loại bỏ noise words (các cụm mào đầu, hỏi giá, dấu câu...) → phần còn lại = tên món.
     *
     * Ví dụ:
     * - "Bên bạn có món Hải Sản Khói Lửa không" → "hải sản khói lửa"
     * - "giá phở bò bao nhiêu?" → "phở bò"
     * - "Thế còn Khoai tây chiên" → "khoai tây chiên"
     * - "cơm tấm sườn bì chả giá sao?" → "cơm tấm sườn bì chả"
     */
    private String extractFoodNameFromMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return null;
        }

        String lowerMessage = message.toLowerCase().trim();

        // Loại bỏ các cụm từ dài trước (để tránh xóa từng từ nhỏ trong cụm)
        // QUAN TRỌNG: sắp xếp từ cụm DÀI → NGẮN để tránh xóa nhầm
        String[] noisePhrases = {
            // Cụm dài — hỏi giá
            "bao nhiêu tiền", "bao nhiêu vậy", "bao nhiêu", "bao nhiu",
            "giá thế nào", "giá bao nhiêu", "giá cả sao", "giá cả",
            "giá bán", "giá sao", "how much", "mắc không", "rẻ không", "đắt không",
            // Cụm dài — mào đầu
            "bên bạn có món", "bên mình có món", "bên em có món",
            "nhà hàng có món", "quán có món", "shop có món", "ở đây có món",
            "bên bạn có", "bên mình có", "bên em có",
            "nhà hàng có", "quán có", "shop có", "ở đây có",
            "cho tôi biết về", "cho tôi biết", "cho biết về", "cho biết",
            "tôi muốn biết về", "tôi muốn biết", "tôi muốn hỏi về", "tôi muốn hỏi",
            "tôi muốn order", "tôi muốn đặt", "tôi muốn gọi", "tôi muốn lấy",
            "tôi muốn ăn", "tôi muốn", "em muốn",
            "thông tin về", "thông tin", "mô tả về", "mô tả",
            "cho hỏi về", "cho hỏi", "hỏi về",
            "cho tôi xin", "cho tôi", "cho xin", "cho em",
            // Cụm follow-up
            "thế còn", "vậy còn", "à còn", "còn có",
            "ngoài ra còn", "ngoài ra",
            // Cụm hỏi tính chất
            "có gì đặc biệt", "gồm những gì", "ngon không", "nên ăn không",
            "nên ăn", "là gì", "là sao", "thế nào",
            // Cụm hành động
            "có bán", "có món",
        };

        String cleaned = lowerMessage;
        for (String phrase : noisePhrases) {
            cleaned = cleaned.replace(phrase, " ");
        }

        // Loại bỏ các từ đơn lẻ còn sót (noise words ngắn)
        String[] noiseWords = {
            "giá", "tiền", "phí", "chi phí",
            "không", "nhỉ", "nhé", "nha", "vậy", "hả", "ha", "ạ", "à",
            "ơi", "này", "đó", "kia", "thế", "rồi", "nè", "hen",
            "order", "đặt", "gọi", "lấy", "muốn", "xin",
            "món", "cái",
        };

        // Loại dấu câu
        cleaned = cleaned.replaceAll("[?!.,;:\"'()\\[\\]{}]", " ");

        // Loại từng noise word (chỉ khớp nguyên từ, tránh xóa substring)
        for (String word : noiseWords) {
            cleaned = cleaned.replaceAll("(?<![\\p{L}])" + java.util.regex.Pattern.quote(word) + "(?![\\p{L}])", " ");
        }

        // Loại bỏ khoảng trắng thừa
        cleaned = cleaned.trim().replaceAll("\\s+", " ");

        // Nếu sau khi loại bỏ noise vẫn còn nội dung → đó là tên món
        if (!cleaned.isEmpty() && cleaned.length() >= 2) {
            return cleaned;
        }

        return null;
    }

    /**
     * Trích xuất từ khóa từ tin nhắn user (cho tìm knowledge base)
     */
    private List<String> extractKeywords(String message) {
        if (message == null || message.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String normalizedMessage = message.toLowerCase().trim();

        Map<String, List<String>> keywordCategories = Map.of(
            "menu", Arrays.asList("thực đơn", "món ăn", "menu", "món", "đồ ăn", "food", "dish"),
            "order", Arrays.asList("đặt hàng", "order", "giao hàng", "delivery", "ship"),
            "payment", Arrays.asList("thanh toán", "payment", "pay", "tiền", "giá", "price", "cost"),
            "time", Arrays.asList("giờ", "time", "mở cửa", "đóng cửa", "hoạt động"),
            "location", Arrays.asList("địa chỉ", "chỗ", "location", "address", "ở đâu"),
            "promotion", Arrays.asList("khuyến mãi", "giảm giá", "promotion", "discount", "sale"),
            "contact", Arrays.asList("liên hệ", "contact", "phone", "email", "hotline")
        );

        List<String> foundKeywords = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : keywordCategories.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (normalizedMessage.contains(keyword)) {
                    foundKeywords.add(keyword);
                }
            }
        }

        if (foundKeywords.isEmpty()) {
            String[] words = normalizedMessage.split("\\s+");
            for (String word : words) {
                if (word.length() > 3) {
                    foundKeywords.add(word);
                }
            }
        }

        return foundKeywords.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Tìm kiếm knowledge base với danh sách từ khóa
     */
    private List<KnowledgeBase> searchKnowledgeBase(List<String> keywords) {
        if (keywords.isEmpty()) {
            return knowledgeBaseRepository.findHighPriorityKnowledge(5);
        }

        Set<KnowledgeBase> results = new HashSet<>();

        for (String keyword : keywords) {
            List<KnowledgeBase> matches = knowledgeBaseRepository.searchByKeyword(keyword);
            results.addAll(matches);
        }

        if (keywords.size() >= 2) {
            for (int i = 0; i < keywords.size() - 1; i++) {
                for (int j = i + 1; j < keywords.size(); j++) {
                    List<KnowledgeBase> combinedMatches = knowledgeBaseRepository
                        .searchByMultipleKeywords(keywords.get(i), keywords.get(j));
                    results.addAll(combinedMatches);
                }
            }
        }

        return results.stream()
            .sorted((a, b) -> {
                int priorityCompare = Integer.compare(b.getPriority(), a.getPriority());
                if (priorityCompare == 0) {
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                }
                return priorityCompare;
            })
            .limit(5)
            .collect(Collectors.toList());
    }

    /**
     * Xây dựng context từ danh sách knowledge base
     */
    private String buildContextFromKnowledge(List<KnowledgeBase> knowledgeList) {
        if (knowledgeList.isEmpty()) {
            return "";
        }

        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("THAM KHẢO TỪ DỮ LIỆU NHÀ HÀNG:\n\n");

        for (int i = 0; i < knowledgeList.size(); i++) {
            KnowledgeBase kb = knowledgeList.get(i);
            contextBuilder.append(String.format("%d. %s (%s)\n",
                i + 1, kb.getTitle(), kb.getCategory().getDisplayName()));
            contextBuilder.append(kb.getContent());
            contextBuilder.append("\n\n");
        }

        contextBuilder.append("---\n");
        contextBuilder.append("Hãy sử dụng thông tin trên để trả lời câu hỏi của khách hàng một cách chính xác và hữu ích. ");
        contextBuilder.append("Nếu thông tin không đủ, hãy thừa nhận và đề xuất liên hệ nhân viên.\n");

        return contextBuilder.toString();
    }

    /**
     * Tìm kiếm knowledge base theo danh mục cụ thể
     */
    public List<KnowledgeBase> searchByCategory(KnowledgeBase.KnowledgeCategory category, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return knowledgeBaseRepository
                .findByCategoryAndIsActiveTrueOrderByPriorityDescCreatedAtDesc(category);
        }

        return knowledgeBaseRepository.findByCategoryAndKeyword(category, keyword.trim());
    }

    /**
     * Lấy context tổng quan về nhà hàng (để tạo system prompt)
     */
    public String getRestaurantOverviewContext() {
        try {
            List<KnowledgeBase> restaurantInfo = knowledgeBaseRepository
                .findByCategoryAndIsActiveTrueOrderByPriorityDescCreatedAtDesc(
                    KnowledgeBase.KnowledgeCategory.RESTAURANT_INFO);

            List<KnowledgeBase> operatingHours = knowledgeBaseRepository
                .findByCategoryAndIsActiveTrueOrderByPriorityDescCreatedAtDesc(
                    KnowledgeBase.KnowledgeCategory.OPERATING_HOURS);

            List<KnowledgeBase> contact = knowledgeBaseRepository
                .findByCategoryAndIsActiveTrueOrderByPriorityDescCreatedAtDesc(
                    KnowledgeBase.KnowledgeCategory.CONTACT);

            StringBuilder overview = new StringBuilder();

            if (!restaurantInfo.isEmpty()) {
                overview.append("THÔNG TIN NHÀ HÀNG:\n");
                restaurantInfo.forEach(info ->
                    overview.append("- ").append(info.getContent()).append("\n"));
                overview.append("\n");
            }

            if (!operatingHours.isEmpty()) {
                overview.append("GIỜ HOẠT ĐỘNG:\n");
                operatingHours.forEach(hours ->
                    overview.append("- ").append(hours.getContent()).append("\n"));
                overview.append("\n");
            }

            if (!contact.isEmpty()) {
                overview.append("THÔNG TIN LIÊN HỆ:\n");
                contact.forEach(contactInfo ->
                    overview.append("- ").append(contactInfo.getContent()).append("\n"));
            }

            return overview.toString();

        } catch (Exception e) {
            log.error("Error fetching overview context: {}", e.getMessage());
            return "Nhà hàng trực tuyến chuyên phục vụ các món ăn ngon, giao hàng nhanh chóng.";
        }
    }

    /**
     * Tính điểm tương đồng giữa câu hỏi và knowledge base (đơn giản)
     */
    public double calculateSimilarity(String query, KnowledgeBase knowledge) {
        if (query == null || knowledge == null) {
            return 0.0;
        }

        String normalizedQuery = query.toLowerCase();
        String combinedKnowledge = (knowledge.getTitle() + " " +
                                  knowledge.getContent() + " " +
                                  knowledge.getKeywords()).toLowerCase();

        String[] queryWords = normalizedQuery.split("\\s+");
        long matchCount = Arrays.stream(queryWords)
            .filter(word -> word.length() > 2)
            .filter(combinedKnowledge::contains)
            .count();

        return (double) matchCount / Math.max(queryWords.length, 1);
    }
}
