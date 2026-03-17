# Copilot Instructions - Food Order Backend (Spring Boot)

## Ngôn ngữ
**Luôn phản hồi bằng Tiếng Việt** trong mọi tình huống.

---

## Tổng quan dự án
- **Tech**: Java Spring Boot, Redis Cache, MySQL, JWT Auth
- **Chức năng**: Xác thực/phân quyền, quản lý món ăn, giỏ hàng, đơn hàng, điểm thưởng, thống kê

---

## Kiến trúc Spring Boot

| Layer | Mô tả | Ví dụ |
|-------|-------|-------|
| **Controller** | Xử lý request/response, định nghĩa endpoint | `FoodController` |
| **Service** | Interface định nghĩa nghiệp vụ | `FoodService` |
| **ServiceImpl** | Triển khai logic nghiệp vụ | `FoodServiceImpl` |
| **Repository** | Tương tác DB, truy vấn dữ liệu | `FoodRepository` |
| **Entity** | Ánh xạ bảng DB | `Food` |
| **DTO** | Request/Response object | `FoodRequest`, `FoodResponse` |

---

## Quy tắc code

### 🔵 Endpoint Convention (Bắt buộc)

Tất cả API **phải** tuân theo convention URL prefix theo vai trò:

| Prefix | Vai trò | Mô tả | Ví dụ |
|--------|---------|-------|-------|
| `/api/v1/public/**` | Khách vãng lai | Không cần đăng nhập - Xem sản phẩm, bài viết, đăng ký, đăng nhập | `/api/v1/public/auth/login`, `/api/v1/public/blogs` |
| `/api/v1/client/**` | Người dùng đã login | Quản lý profile, đặt hàng, xem lịch sử cá nhân | `/api/v1/client/orders`, `/api/v1/client/profile` |
| `/api/v1/staff/**` | Nhân viên | Xác nhận đơn hàng, cập nhật trạng thái | `/api/v1/staff/orders`, `/api/v1/staff/foods/status` |
| `/api/v1/admin/**` | Quản trị viên | Quản lý nhân sự, cấu hình hệ thống, thống kê | `/api/v1/admin/blogs`, `/api/v1/admin/users` |

**Phân quyền tự động trong SecurityConfig:**
- `/api/v1/public/**` → `permitAll()`
- `/api/v1/client/**` → `authenticated()`
- `/api/v1/staff/**` → `hasAnyRole("STAFF", "ADMIN")`
- `/api/v1/admin/**` → `hasRole("ADMIN")`

**Trạng thái migration:**
| Module | Cũ | Mới | Trạng thái |
|--------|----|-----|------------|
| Auth | `/api/auth/**` | `/api/v1/public/auth/**` | ✅ Done |
| Blog Public | `/api/blogs/**` | `/api/v1/public/blogs/**` | ✅ Done |
| Blog Admin | `/api/admin/blogs/**` | `/api/v1/admin/blogs/**` | ✅ Done |
| Cart | `/api/cart/**` | `/api/v1/client/cart/**` | ✅ Done |
| Category Public | `/api/categories/**` (GET) | `/api/v1/public/categories/**` | ✅ Done |
| Category Admin | `/api/categories/**` (CUD) | `/api/v1/admin/categories/**` | ✅ Done |
| Food | `/api/foods/**` | `/api/v1/public/foods/**`, `/api/v1/staff/foods/**`, `/api/v1/admin/foods/**` | ✅ Done |
| Order | `/api/orders/**`, `/api/staff/orders/**`, `/api/admin/orders/**` | `/api/v1/public/orders/**`, `/api/v1/client/orders/**`, `/api/v1/staff/orders/**`, `/api/v1/admin/orders/**` | ✅ Done |
| User | `/api/users/**`, `/api/admin/users/**` | `/api/v1/client/users/**`, `/api/v1/admin/users/**` | ✅ Done |
| Comment | `/api/comments/**`, `/api/admin/comments/**` | `/api/v1/public/comments/**`, `/api/v1/client/comments/**`, `/api/v1/admin/comments/**` | ✅ Done |
| Contact | `/api/contact/**`, `/api/admin/contacts/**` | `/api/v1/public/contact`, `/api/v1/staff/contacts/**` | ✅ Done |
| Coupon | `/api/coupons/**`, `/api/admin/coupons/**` | `/api/v1/public/coupons/**`, `/api/v1/client/coupons/**`, `/api/v1/admin/coupons/**` | ✅ Done |
| Chat | `/api/chat/**` | `/api/v1/client/chat/**`, `/api/v1/staff/chat/**`, `/api/v1/admin/chat/**` | ✅ Done |
| Chatbot | `/api/chatbot/**` | `/api/v1/public/chatbot/**` | ✅ Done |
| Dashboard | `/api/admin/dashboard/**` | `/api/v1/staff/dashboard/**` | ✅ Done |
| Favorite | `/api/favorites/**` | `/api/v1/client/favorites/**` | ✅ Done |
| Feedback | `/api/feedback-media/**` | `/api/v1/public/feedback-media/**`, `/api/v1/admin/feedback-media/**` | ✅ Done |
| Like | `/api/likes/**` | `/api/v1/public/likes/**`, `/api/v1/client/likes/**` | ✅ Done |
| Notification | `/api/notifications/**` | `/api/v1/client/notifications/**`, `/api/v1/staff/notifications/**` | ✅ Done |
| Points | `/api/points/**` | `/api/v1/client/points/**` | ✅ Done |
| Restaurant | `/api/admin/restaurant/**` | `/api/v1/admin/restaurant/**` | ✅ Done |
| Payments | `/api/payments/**` | `/api/v1/public/payments/**` | ✅ Done |
| Search | `/api/v1/search` | `/api/v1/public/search`, `/api/v1/admin/search/**` | ✅ Done |
| Share | `/api/shares/**` | `/api/v1/public/shares/**` | ✅ Done |
| Zone | `/api/districts/**`, `/api/wards/**` | `/api/v1/public/districts/**`, `/api/v1/public/wards/**` | ✅ Done |

### Cấu trúc & Convention
- Import đặt ở **đầu file**
- Endpoint RESTful theo convention: `/api/v1/{role}/{resource}`
- Sử dụng `@Valid` cho validation DTO
- Comment rõ ràng cho logic phức tạp
- Phân quyền: `@PreAuthorize`, `@RequireStaff`, `@RequireAdmin`

### Error Handling
- Sử dụng `GlobalExceptionHandler`
- Trả về **errorCode chuẩn hóa**: `FOOD_NOT_FOUND`, `INVALID_CREDENTIALS`, `EMAIL_NOT_VERIFIED`
- Không trả message tự do, FE dựa vào errorCode để hiển thị

### Bảo mật
- Không tự ý sửa file `.env`
- Kiểm tra xác thực/phân quyền trước thao tác nhạy cảm
- Sử dụng biến môi trường cho thông tin bảo mật

---

## 🔴 QUAN TRỌNG: Cache với Redis

### Khi nào cần Cache?
| Loại API | Cần Cache? | TTL đề xuất |
|----------|------------|-------------|
| GET danh sách public (foods, blogs) | ✅ Có | 5 phút |
| GET chi tiết (food detail, blog detail) | ✅ Có | 5 phút |
| GET thống kê dashboard | ✅ Có | 10-15 phút |
| GET danh mục, config ít thay đổi | ✅ Có | 30 phút |
| GET comments, tương tác nhiều | ✅ Có | 3 phút |
| POST/PUT/DELETE | ❌ Không cache | - |

### TTL (Time To Live) Guidelines
```
TTL_SHORT = 3 phút    → Dữ liệu thay đổi thường xuyên (comments)
TTL_DEFAULT = 5 phút  → Dữ liệu chi tiết, danh sách
TTL_MEDIUM = 10 phút  → Danh sách admin, thống kê
TTL_LONG = 15 phút    → Dashboard, reports
TTL_VERY_LONG = 30 phút → Danh mục, config ít thay đổi
```

### Cách triển khai Cache

**1. Thêm cache constant vào `CacheConfig.java`:**
```java
public static final String MY_CACHE = "myCache";
// Thêm vào cacheConfigurations:
cacheConfigurations.put(MY_CACHE, defaultConfig.entryTtl(TTL_DEFAULT));
```

**2. Thêm @Cacheable cho GET methods:**
```java
@Cacheable(value = CacheConfig.MY_CACHE, key = "#id")
public MyResponse getById(Long id) { ... }

// Với phân trang:
@Cacheable(value = CacheConfig.MY_CACHE, 
           key = "#pageable.pageNumber + '_' + #pageable.pageSize")
public Page<MyResponse> getAll(Pageable pageable) { ... }
```

**3. Thêm @CacheEvict cho CUD methods:**
```java
@Caching(evict = {
    @CacheEvict(value = CacheConfig.MY_CACHE, allEntries = true),
    @CacheEvict(value = CacheConfig.MY_DETAIL_CACHE, allEntries = true)
})
public MyResponse create(MyRequest request) { ... }
```

**4. DTO phải implement Serializable:**
```java
public class MyResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    // fields...
}
```

---

## Tài liệu
- **Mỗi lần thêm/sửa chức năng** → Cập nhật hoặc tạo file `.md` trong `/docs`

---

## Checklist khi tạo API mới

- [ ] Tách đúng Controller → Service → ServiceImpl → Repository
- [ ] Sử dụng DTO cho request/response (implement Serializable nếu cần cache)
- [ ] Validate với `@Valid`
- [ ] Phân quyền phù hợp (`@RequireStaff`, `@RequireAdmin`)
- [ ] Error trả về errorCode chuẩn
- [ ] **Xem xét thêm Cache** cho GET APIs
- [ ] Cập nhật tài liệu trong `/docs`
