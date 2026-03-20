# Copilot Instructions - Dong Xanh Food Order Backend

## Ngôn ngữ
**Luôn phản hồi bằng Tiếng Việt** trong mọi tình huống.

---

## 1. Tổng quan dự án

### Mô tả
Hệ thống đặt món ăn trực tuyến cho nhà hàng **Dong Xanh Food** — bao gồm website hiển thị thực đơn, đặt hàng, thanh toán online, quản lý đơn hàng real-time, chatbot AI hỗ trợ khách hàng, hệ thống blog/tin tức, và dashboard thống kê nâng cao.

### Tech Stack
| Thành phần | Công nghệ |
|-----------|-----------|
| **Framework** | Java 17, Spring Boot 3.2.5 |
| **Database** | MySQL 8.0 |
| **Cache** | Redis 7 (Spring Data Redis) |
| **Authentication** | JWT (jjwt 0.11.5), OAuth2 (Google, Facebook) |
| **Real-time** | WebSocket + STOMP (SockJS fallback) |
| **File Storage** | AWS S3 |
| **Email** | Brevo (Sendinblue) API + Thymeleaf templates |
| **Search** | Algolia Full-text Search |
| **AI Chatbot** | OpenAI GPT-4o + RAG (Knowledge Base) |
| **Payment** | ZaloPay, MoMo, COD |
| **API Docs** | SpringDoc OpenAPI 3 (Swagger UI) |
| **Deployment** | Docker Compose, AWS Lightsail |
| **Khác** | ModelMapper, Lombok, WebFlux (reactive HTTP client) |

### Domain
- Production: `dongxanhfood.shop` / `dongxanhfoodorder.shop`
- Frontend: Firebase Hosting (`oder-4c1f2.web.app`)

---

## 2. Kiến trúc tổng quát

### Layer Architecture
```
Controller → Service (Interface) → ServiceImpl → Repository → Entity
                                                              ↕
                                                           DTO (Request/Response)
```

| Layer | Mô tả | Ví dụ |
|-------|-------|-------|
| **Controller** | Xử lý HTTP request/response, định nghĩa endpoint | `FoodPublicController`, `FoodAdminController` |
| **Service** | Interface định nghĩa nghiệp vụ | `FoodService`, `OrderService` |
| **ServiceImpl** | Triển khai logic nghiệp vụ, cache, validation | `FoodServiceImpl`, `OrderCoreService` |
| **Repository** | Tương tác DB (Spring Data JPA) | `FoodRepository`, `OrderRepository` |
| **Entity** | Ánh xạ bảng DB (JPA Entity) | `Food`, `Order`, `User` |
| **DTO** | Request/Response object, tách biệt với Entity | `FoodRequest`, `FoodResponse` |

### Package Structure
```
com.foodorder.backend/
├── auth/           # Xác thực (login, register, verify email, reset password)
├── blog/           # Bài viết, tin tức, danh mục blog
├── cart/           # Giỏ hàng
├── category/       # Danh mục món ăn (cây phân cấp)
├── chat/           # Chat real-time User ↔ Staff (WebSocket)
├── chatbot/        # AI Chatbot (OpenAI + RAG)
├── comment/        # Bình luận (polymorphic: Food, Blog)
├── config/         # Cấu hình (Security, Cache, WebSocket, S3, Algolia...)
├── contact/        # Liên hệ từ khách hàng
├── coupons/        # Mã giảm giá (hệ thống coupon nâng cao)
├── dashboard/      # Thống kê, báo cáo cho Admin/Staff
├── exception/      # Xử lý lỗi tập trung (GlobalExceptionHandler)
├── favorite/       # Món ăn yêu thích
├── feedbacks/      # Feedback media (ảnh/video review)
├── food/           # Quản lý món ăn (CRUD, trạng thái, variants, images)
├── like/           # Lượt thích (polymorphic: Food, Blog)
├── notifications/  # Thông báo cho User và Staff
├── order/          # Đơn hàng (đặt hàng, tracking, quản lý)
├── payments/       # Thanh toán (ZaloPay, MoMo, COD)
├── points/         # Điểm thưởng (tích/dùng/hoàn điểm)
├── restaurant/     # Thông tin nhà hàng + gallery
├── search/         # Tìm kiếm Algolia
├── security/       # JWT filter, OAuth2 handlers, annotations
├── service/        # Shared services (Email, S3, WebSocket, Thymeleaf)
├── share/          # Lượt chia sẻ
├── user/           # Quản lý người dùng + nhân viên
├── util/           # Utility (slug, currency formatter, migration runner)
├── validation/     # Custom validators (ValidPassword)
├── websocket/      # WebSocket controllers (Staff, User)
└── zone/           # Quận/huyện, phường/xã (phí giao hàng)
```

---

## 3. Hệ thống phân quyền (RBAC)

### Roles
| Role | Code (DB) | Mô tả |
|------|-----------|-------|
| Khách hàng | `ROLE_USER` | Đặt hàng, xem lịch sử, chat |
| Nhân viên | `ROLE_STAFF` | Xác nhận đơn, cập nhật trạng thái, chat với khách |
| Quản trị viên | `ROLE_ADMIN` | Toàn quyền quản lý hệ thống |

### Endpoint Convention (Bắt buộc)

| Prefix | Vai trò | SecurityConfig | Ví dụ |
|--------|---------|---------------|-------|
| `/api/v1/public/**` | Khách vãng lai | `permitAll()` | `/api/v1/public/foods`, `/api/v1/public/auth/login` |
| `/api/v1/client/**` | Người dùng đã login | `authenticated()` | `/api/v1/client/orders`, `/api/v1/client/cart` |
| `/api/v1/staff/**` | Nhân viên + Admin | `hasAnyRole("STAFF", "ADMIN")` | `/api/v1/staff/orders`, `/api/v1/staff/dashboard` |
| `/api/v1/admin/**` | Quản trị viên | `hasRole("ADMIN")` | `/api/v1/admin/foods`, `/api/v1/admin/users` |

### Custom Annotations
- `@RequireAdmin` — Yêu cầu role ADMIN
- `@RequireStaff` — Yêu cầu role STAFF hoặc ADMIN
- `@RequireCustomer` — Yêu cầu role USER
- `@RequireSuperAdmin` — Bảo vệ dữ liệu đặc biệt (`isProtected = true`)

---

## 4. Chi tiết các Module đã triển khai

### 4.1. Auth (`auth/`)
**Trạng thái: ✅ Hoàn thành**

| Tính năng | Endpoint | Mô tả |
|----------|---------|-------|
| Đăng ký | `POST /api/v1/public/auth/register` | Đăng ký + gửi email xác thực |
| Đăng nhập | `POST /api/v1/public/auth/login` | Login bằng email/username → trả JWT |
| Xác thực email | `GET /api/v1/public/auth/verify?token=` | Verify email qua token (24h) |
| Quên mật khẩu | `POST /api/v1/public/auth/forgot-password` | Gửi link reset password |
| Reset mật khẩu | `POST /api/v1/public/auth/reset-password` | Đặt lại mật khẩu bằng token (1h) |
| OAuth2 Google | `GET /oauth2/authorization/google` | Đăng nhập qua Google |
| OAuth2 Facebook | `GET /oauth2/authorization/facebook` | Đăng nhập qua Facebook |

- **Entity**: `User`, `UserToken` (EMAIL_VERIFICATION, PASSWORD_RESET), `ChangePasswordAttempt`
- **Email templates**: `verification_email.html`, `reset_password_email.html`

### 4.2. User Management (`user/`)
**Trạng thái: ✅ Hoàn thành**

| Tính năng | Endpoint | Mô tả |
|----------|---------|-------|
| Xem/Sửa profile | `GET/PUT /api/v1/client/users/profile` | Quản lý thông tin cá nhân |
| Đổi mật khẩu | `PUT /api/v1/client/users/change-password` | Đổi mật khẩu (có check mật khẩu cũ) |
| Quản lý users (Admin) | `GET /api/v1/admin/users` | Danh sách users + phân trang |
| Quản lý nhân viên | `GET /api/admin/employees` | CRUD nhân viên |
| Khóa/Mở tài khoản | `PATCH /api/v1/admin/users/{id}/status` | Active/Inactive user |

- **Entity**: `User` (username, email, fullName, phone, avatar, address, authProvider, role, isActive, isVerified, isProtected, rewardPoint)
- **Roles**: `Role` (id, code, name)

### 4.3. Food (`food/`)
**Trạng thái: ✅ Hoàn thành**

**Public APIs** (`/api/v1/public/foods`):
| Tính năng | Endpoint |
|----------|---------|
| Tất cả món ăn | `GET /` |
| Món mới | `GET /new` |
| Món nổi bật | `GET /featured` |
| Bestsellers | `GET /bestsellers` |
| Theo danh mục (ID) | `GET /by-category/{categoryId}` |
| Theo danh mục (slug) | `GET /by-category-slug/{slug}` |
| Chi tiết (slug) | `GET /slug/{slug}` |
| Chi tiết (ID) | `GET /{id}` |

**Admin APIs** (`/api/v1/admin/foods`):
| Tính năng | Endpoint |
|----------|---------|
| Danh sách quản lý | `GET /management` |
| Chi tiết | `GET /{id}` |
| Tạo mới | `POST /` (multipart) |
| Cập nhật | `PUT /{id}` (multipart) |
| Xóa | `DELETE /{id}` |
| Upload ảnh | `POST /upload` |
| Cập nhật trạng thái | `PATCH /{id}/status` |

**Staff APIs** (`/api/v1/staff/foods`):
| Tính năng | Endpoint |
|----------|---------|
| Danh sách quản lý | `GET /management` |
| Cập nhật trạng thái | `PATCH /{id}/status` |

- **Entity**: `Food` (name, slug, description, price, imageUrl, isBestSeller, isNew, isFeatured, totalSold, stockQuantity, totalLikes, totalShares, isActive, isProtected, status, statusNote, category)
- **Liên kết**: `FoodImage` (nhiều ảnh), `FoodVariant` (nhiều biến thể: size, topping...)
- **Trạng thái**: `AVAILABLE`, `UNAVAILABLE`

### 4.4. Category (`category/`)
**Trạng thái: ✅ Hoàn thành**

| Tính năng | Endpoint |
|----------|---------|
| Danh mục gốc | `GET /api/v1/public/categories/roots` |
| Danh mục con | `GET /api/v1/public/categories/by-parent/{parentId}` |
| Tạo mới (Admin) | `POST /api/v1/admin/categories` |
| Cập nhật (Admin) | `PUT /api/v1/admin/categories/{id}` |
| Xóa (Admin) | `DELETE /api/v1/admin/categories/{id}` |

- **Entity**: `Category` (name, slug, description, parentId, displayOrder) — Cây phân cấp (parent-child)

### 4.5. Cart (`cart/`)
**Trạng thái: ✅ Hoàn thành**

| Tính năng | Endpoint |
|----------|---------|
| Thêm vào giỏ | `POST /api/v1/client/cart/add` |
| Cập nhật số lượng | `POST /api/v1/client/cart/update` |
| Xóa khỏi giỏ | `DELETE /api/v1/client/cart/remove` |
| Xem giỏ hàng | `GET /api/v1/client/cart` |
| Xóa toàn bộ | `DELETE /api/v1/client/cart/clear` |

- **Entity**: `CartItem` (userId, foodId, variantId, quantity)

### 4.6. Order (`order/`)
**Trạng thái: ✅ Hoàn thành**

**Workflow đơn hàng:**
```
PENDING → PROCESSING → CONFIRMED → DELIVERING → COMPLETED
    ↓         ↓            ↓           ↓
  CANCELLED  CANCELLED   CANCELLED   CANCELLED
```

**Public APIs** (`/api/v1/public/orders`):
| Tính năng | Endpoint |
|----------|---------|
| Đặt hàng (khách vãng lai) | `POST /` |
| Tra cứu đơn hàng | `GET /track/{orderCode}` |

**Client APIs** (`/api/v1/client/orders`):
| Tính năng | Endpoint |
|----------|---------|
| Đặt hàng (đã login) | `POST /` |
| Lịch sử đơn hàng | `GET /` |
| Chi tiết đơn | `GET /{id}` |
| Hủy đơn | `PATCH /{id}/cancel` |

**Staff APIs** (`/api/v1/staff/orders`):
| Tính năng | Endpoint |
|----------|---------|
| Danh sách đơn | `GET /` |
| Cập nhật trạng thái | `PATCH /{id}/status` |

**Admin APIs** (`/api/v1/admin/orders`):
| Tính năng | Endpoint |
|----------|---------|
| Danh sách đơn + filter | `GET /` |
| Thống kê đơn hàng | `GET /statistics` |

- **Entity**: `Order` (receiverName, receiverPhone, receiverEmail, deliveryAddress, paymentMethod, deliveryType, status, subtotalAmount, shippingFee, totalBeforeDiscount, finalAmount, pointsUsed, pointsDiscountAmount, couponCode, couponDiscountAmount, orderCode, staffNote, cancelReason)
- **Liên kết**: `OrderItem`, `OrderTracking` (lịch sử trạng thái)
- **Delivery Types**: `DELIVERY` (giao hàng), `TAKE_AWAY` (tự đến lấy), `DINE_IN` (ăn tại chỗ)
- **Payment Methods**: `COD`, `ZALOPAY`, `MOMO`, `VNPAY`, `BANKING`, `ATM`, `VISA`
- **Payment Status**: `PENDING`, `PAID`, `FAILED`, `REFUNDED`
- **Mã đơn hàng**: Tự động tạo format `DGX{6 digits}{3 digits}` (ví dụ: `DGX123456789`)
- **Email thông báo**: Gửi email khi đặt hàng thành công, hoàn thành, hủy đơn

### 4.7. Payments (`payments/`)
**Trạng thái: ✅ Hoàn thành**

| Tính năng | Endpoint |
|----------|---------|
| Tạo thanh toán | `POST /api/v1/public/payments/create` |
| Callback ZaloPay | `POST /api/v1/public/payments/zalopay/callback` |
| Callback MoMo | `POST /api/v1/public/payments/momo/callback` |
| Kiểm tra trạng thái | `GET /api/v1/public/payments/status/{orderId}` |

- **Tích hợp**: ZaloPay (QR code, ZaloPay wallet), MoMo
- **Service**: `ZaloPayPaymentService`, `MomoPaymentService`

### 4.8. Coupons (`coupons/`)
**Trạng thái: ✅ Hoàn thành**

**Public APIs** (`/api/v1/public/orders/coupon`):
| Tính năng | Endpoint |
|----------|---------|
| Kiểm tra coupon | `POST /validate` |

**Client APIs** (`/api/v1/client/coupons`):
| Tính năng | Endpoint |
|----------|---------|
| Coupon khả dụng | `GET /available` |

**Admin APIs** (`/api/v1/admin/coupons`):
| Tính năng | Endpoint |
|----------|---------|
| CRUD coupon | `GET/POST/PUT/DELETE /` |
| Thống kê coupon | `GET /statistics` |

- **Entity**: `Coupon` (code, title, description, discountType, discountValue, minOrderAmount, maxDiscountAmount, maxUsagePerUser, startDate, endDate, maxUsage, usedCount, status, couponType)
- **Loại giảm**: `PERCENT`, `AMOUNT`
- **Loại coupon**: `PUBLIC`, `PRIVATE`, `FIRST_ORDER`
- **Trạng thái**: `ACTIVE`, `INACTIVE`, `EXPIRED`
- **Liên kết**: Áp dụng theo `Category`, theo `Food`, theo `User` riêng (ManyToMany)
- **Scheduler**: `CouponScheduler` — Tự động cập nhật trạng thái coupon hết hạn
- **Tracking**: `CouponUsage` — Lưu vết sử dụng coupon

### 4.9. Points / Reward Points (`points/`)
**Trạng thái: ✅ Hoàn thành**

| Tính năng | Endpoint |
|----------|---------|
| Xem điểm + lịch sử | `GET /api/v1/client/points` |
| Lịch sử điểm | `GET /api/v1/client/points/history` |
| Thống kê (Admin) | `GET /api/v1/admin/promotions/points/statistics` |

- **Entity**: `RewardPoint` (user, balance), `PointHistory` (userId, type, amount, orderId, description), `PointRule`
- **Loại điểm**: `EARN` (tích điểm khi mua), `USE` (dùng điểm giảm giá), `REFUND` (hoàn điểm khi hủy đơn), `EXPIRE`
- **Quy tắc**: Tích điểm theo đơn hàng hoàn thành, dùng điểm khi đặt hàng

### 4.10. Blog (`blog/`)
**Trạng thái: ✅ Hoàn thành**

**Public APIs** (`/api/v1/public/blogs`):
| Tính năng | Endpoint |
|----------|---------|
| Danh sách bài viết | `GET /` |
| Bài viết nổi bật | `GET /featured` |
| Theo loại (type) | `GET /by-type/{type}` |
| Theo danh mục | `GET /by-category/{categoryId}` |
| Chi tiết (slug) | `GET /slug/{slug}` |
| Bài liên quan | `GET /{id}/related` |
| Danh mục blog | `GET /categories` |

**Admin APIs** (`/api/v1/admin/blogs`):
| Tính năng | Endpoint |
|----------|---------|
| CRUD bài viết | `GET/POST/PUT/DELETE /` |
| CRUD danh mục | `GET/POST/PUT/DELETE /categories` |
| Đổi trạng thái | `PATCH /{id}/status` |

- **Entity**: `Blog` (title, slug, summary, content (LONGTEXT), thumbnail, status, blogType, viewCount, isFeatured, isProtected, tags, SEO fields)
- **Blog Types**: `NEWS_PROMOTIONS`, `MEDIA_PRESS`, `CATERING_SERVICES`
- **Blog Status**: `DRAFT`, `PUBLISHED`, `ARCHIVED`
- **Liên kết**: `BlogCategory` (danh mục blog riêng)

### 4.11. Comment (`comment/`)
**Trạng thái: ✅ Hoàn thành**

| Tính năng | Endpoint |
|----------|---------|
| Xem comments | `GET /api/v1/public/comments?targetType=&targetId=` |
| Đếm comments | `GET /api/v1/public/comments/count` |
| Tạo comment | `POST /api/v1/client/comments` |
| Reply comment | `POST /api/v1/client/comments/{parentId}/reply` |
| Quản lý (Admin) | `GET/DELETE /api/v1/admin/comments` |

- **Entity**: `Comment` (user, content, targetType, targetId, parentId) — Polymorphic + tree structure
- **Target**: `FOOD`, `BLOG` (dùng chung `TargetType` với Like/Share)
- **Trạng thái**: `CommentStatus` (ACTIVE, HIDDEN, DELETED)

### 4.12. Like & Share (`like/`, `share/`)
**Trạng thái: ✅ Hoàn thành**

| Tính năng | Endpoint |
|----------|---------|
| Like/Unlike | `POST /api/v1/client/likes/toggle` |
| Kiểm tra đã like | `GET /api/v1/client/likes/check` |
| Đếm likes (public) | `GET /api/v1/public/likes/count` |
| Ghi nhận share | `POST /api/v1/public/shares` |

- **Entity**: `Like` (user, targetType, targetId), `Share` (user, targetType, targetId, platform)
- **Polymorphic**: Hỗ trợ like/share cho `FOOD`, `BLOG`, `MOVIE` (extensible)
- **Share platforms**: `SharePlatform` enum (FACEBOOK, TWITTER, ZALO...)

### 4.13. Favorite (`favorite/`)
**Trạng thái: ✅ Hoàn thành**

| Tính năng | Endpoint |
|----------|---------|
| Toggle yêu thích | `POST /api/v1/client/favorites/toggle` |
| Danh sách yêu thích | `GET /api/v1/client/favorites` |

- **Entity**: `FavoriteFood` (user, food, variant) — Unique constraint (user + food + variant)

### 4.14. Chat User ↔ Staff (`chat/`)
**Trạng thái: ✅ Hoàn thành**

**REST APIs**:
| Tính năng | Endpoint |
|----------|---------|
| Lịch sử chat (User) | `GET /api/v1/client/chat/history` |
| Tin nhắn chưa đọc | `GET /api/v1/client/chat/unread-count` |
| Danh sách conversations (Staff) | `GET /api/v1/staff/chat/conversations` |
| Lịch sử chat (Staff) | `GET /api/v1/staff/chat/history/{userId}` |
| Quản lý chat (Admin) | `GET /api/v1/admin/chat/...` |

**WebSocket** (STOMP):
| Channel | Mô tả |
|---------|-------|
| `/app/chat.send` | User gửi tin nhắn |
| `/app/staff.chat.send` | Staff gửi tin nhắn |
| `/topic/chat/{conversationId}` | Nhận tin nhắn real-time |

- **Entity**: `Conversation` (user, lastMessageAt, unreadCountUser, unreadCountStaff), `ChatMessage` (conversation, sender, content, isRead)
- **Mô hình**: Mỗi User có duy nhất 1 conversation với Staff

### 4.15. AI Chatbot (`chatbot/`)
**Trạng thái: ✅ Hoàn thành**

| Tính năng | Endpoint |
|----------|---------|
| Chat với bot | `POST /api/v1/public/chatbot/chat` |
| Lịch sử chat | `GET /api/v1/public/chatbot/history/{sessionId}` |

- **Architecture**: RAG (Retrieval-Augmented Generation)
  - `KnowledgeBase` entity — Lưu thông tin nhà hàng, menu, chính sách, FAQ
  - `RAGService` — Tìm kiếm context liên quan từ knowledge base
  - `OpenAIService` — Gọi GPT-4o với context
  - `MenuInfoService` — Cung cấp thông tin thực đơn real-time
  - `ChatbotInitializer` — Khởi tạo knowledge base khi start
- **Entity**: `ChatbotMessage` (sessionId, role, content), `KnowledgeBase` (title, content, keywords, category, priority)
- **Knowledge Categories**: `RESTAURANT_INFO`, `MENU_INFO`, `ORDER_POLICY`, `PAYMENT_INFO`, `DELIVERY_INFO`, `PROMOTION`, `FAQ`

### 4.16. Contact (`contact/`)
**Trạng thái: ✅ Hoàn thành**

| Tính năng | Endpoint |
|----------|---------|
| Gửi liên hệ | `POST /api/v1/public/contacts` |
| Danh sách (Admin) | `GET /api/v1/admin/contacts` |
| Phản hồi (Admin) | `POST /api/v1/admin/contacts/{id}/reply` |

- **Entity**: `ContactMessage` (name, email, phone, subject, message, status)
- **Trạng thái**: `PENDING`, `READ`, `REPLIED`, `ARCHIVED`

### 4.17. Notifications (`notifications/`)
**Trạng thái: ✅ Hoàn thành**

| Tính năng | Endpoint |
|----------|---------|
| Thông báo của User | `GET /api/v1/client/notifications` |
| Đánh dấu đã đọc | `PATCH /api/v1/client/notifications/{id}/read` |
| Thông báo của Staff | `GET /api/v1/staff/notifications` |

- **Entity**: `Notification` (userId, orderId, orderCode, title, message, type, recipientType, recipientId, isRead)
- **Recipient Types**: `USER`, `STAFF`
- **WebSocket**: Gửi notification real-time qua `/topic/notifications`

### 4.18. Dashboard & Statistics (`dashboard/`)
**Trạng thái: ✅ Hoàn thành**

| Tính năng | Endpoint |
|----------|---------|
| Thống kê tổng quan | `GET /api/v1/staff/dashboard/statistics` |
| Doanh thu theo ngày | `GET /api/v1/staff/dashboard/revenue?days=7` |
| Hoạt động gần đây | `GET /api/v1/staff/dashboard/activities` |
| Top món bán chạy | `GET /api/v1/staff/dashboard/top-selling-foods?period=7` |
| Thống kê nâng cao | `GET /api/v1/staff/dashboard/advanced-statistics` |
| Doanh thu theo danh mục | `GET /api/v1/staff/dashboard/revenue-by-category` |
| Hiệu quả món ăn | `GET /api/v1/staff/dashboard/food-performance` |

- **Thống kê**: Tổng khách hàng, doanh thu tháng, đơn hàng hôm nay, số nhân viên
- **Nâng cao**: AOV (Average Order Value), tỷ lệ hủy đơn, điểm thưởng đã dùng, xu hướng

### 4.19. Restaurant Info (`restaurant/`)
**Trạng thái: ✅ Hoàn thành**

| Tính năng | Endpoint |
|----------|---------|
| Xem thông tin | `GET /api/v1/public/restaurant` |
| Cập nhật (Admin) | `PUT /api/v1/admin/restaurant` |

- **Entity**: `RestaurantInfo` (name, logoUrl, address, phoneNumber, videoUrl, description, openingHours), `RestaurantGallery` (imageUrl, displayOrder)

### 4.20. Feedback Media (`feedbacks/`)
**Trạng thái: ✅ Hoàn thành**

| Tính năng | Endpoint |
|----------|---------|
| Xem feedback | `GET /api/v1/public/feedbacks` |
| Quản lý (Admin) | `GET/POST/PUT/DELETE /api/v1/admin/feedbacks` |

- **Entity**: `FeedbackMedia` (type: IMAGE/VIDEO, mediaUrl, thumbnailUrl, displayOrder)

### 4.21. Search (`search/`)
**Trạng thái: ✅ Hoàn thành**

| Tính năng | Endpoint |
|----------|---------|
| Tìm kiếm món ăn | `GET /api/v1/public/search?query=&page=&hitsPerPage=` |
| Đồng bộ Algolia (Admin) | `POST /api/v1/admin/search/sync` |

- **Engine**: Algolia full-text search
- **Service**: `AlgoliaSearchService` — Đồng bộ data giữa MySQL ↔ Algolia

### 4.22. Zone (`zone/`)
**Trạng thái: ✅ Hoàn thành**

| Tính năng | Endpoint |
|----------|---------|
| Danh sách quận/huyện | `GET /api/v1/public/districts` |
| Phường/xã theo quận | `GET /api/v1/public/wards/by-district/{districtId}` |

- **Entity**: `District` (name, deliveryFee, isActive), `Ward` (name, district)
- **Mục đích**: Tính phí giao hàng theo khu vực

### 4.23. WebSocket (`websocket/`)
**Trạng thái: ✅ Hoàn thành**

| Component | Mô tả |
|-----------|-------|
| `UserWebSocketController` | Xử lý message từ User |
| `StaffWebSocketController` | Xử lý message từ Staff |
| `WebSocketEventListener` | Theo dõi connect/disconnect |
| `WebSocketConfig` | Cấu hình STOMP endpoints `/ws`, `/ws/staff-orders` |

- **Sử dụng cho**: Chat real-time, thông báo đơn hàng mới, cập nhật trạng thái đơn

---

## 5. Shared Services (`service/`)

| Service | Mô tả |
|---------|-------|
| `BrevoEmailService` | Gửi email qua Brevo API (xác thực, reset password, đơn hàng) |
| `S3Service` | Upload/delete file trên AWS S3 |
| `ThymeleafTemplateService` | Render HTML template cho email |
| `WebSocketService` | Gửi message qua WebSocket |

### Email Templates (`templates/`)
| Template | Mục đích |
|---------|---------|
| `verification_email.html` | Email xác thực tài khoản |
| `reset_password_email.html` | Email reset mật khẩu |
| `order_success_email.html` | Xác nhận đơn hàng |
| `order_completed_email.html` | Đơn hàng hoàn thành |
| `order_cancelled_email.html` | Đơn hàng bị hủy |

---

## 6. Error Handling

### Cấu trúc lỗi chuẩn
```json
{
  "status": 400,
  "message": "Mô tả lỗi",
  "errorCode": "FOOD_NOT_FOUND",
  "errors": null,
  "details": null
}
```

### Exception Classes
| Exception | HTTP Status | Mô tả |
|-----------|------------|-------|
| `BadRequestException` | 400 | Dữ liệu không hợp lệ |
| `ResourceNotFoundException` | 404 | Không tìm thấy tài nguyên |
| `ForbiddenException` | 403 | Không có quyền (dữ liệu bảo vệ) |
| `TooManyRequestException` | 429 | Quá nhiều request |
| `JwtTokenExpiredException` | 401 | Token hết hạn |
| `JwtTokenInvalidException` | 401 | Token không hợp lệ |

### Quy tắc
- FE dựa vào **errorCode** để hiển thị, KHÔNG dùng message trực tiếp
- Ví dụ errorCode: `FOOD_NOT_FOUND`, `INVALID_CREDENTIALS`, `EMAIL_NOT_VERIFIED`, `PROTECTED_DATA`, `CHATBOT_ERROR`

---

## 7. Cache với Redis

### TTL Guidelines
| TTL | Thời gian | Dùng cho |
|-----|-----------|---------|
| `TTL_SHORT` | 3 phút | Comments (thay đổi thường xuyên) |
| `TTL_DEFAULT` | 5 phút | Danh sách, chi tiết món ăn, đơn hàng |
| `TTL_MEDIUM` | 10 phút | Danh sách admin, thống kê |
| `TTL_LONG` | 15 phút | Dashboard, reports |
| `TTL_VERY_LONG` | 30 phút | Danh mục, blog categories, restaurant info |
| `TTL_EXTRA_LONG` | 1 giờ | Quận/huyện, phường/xã (hầu như không đổi) |

### Cache đã triển khai
Tất cả cache constants được định nghĩa trong `CacheConfig.java`:
- **Food**: `foodsAll`, `foodsNew`, `foodsFeatured`, `foodsBestseller`, `foodsByCategory`, `foodDetail`, `foodDetailSlug`, `foodsManagement`, `adminFoods`, `adminFoodDetails`
- **Order**: `orderStatistics`, `adminOrders`
- **Dashboard**: `dashboardStatistics`, `dashboardRevenue`, `dashboardActivities`, `topSellingFoods`, `advancedStatistics`, `revenueByCategory`, `foodPerformance`
- **Blog**: `blogs`, `featuredBlogs`, `blogCategories`, `blogsByType`, `blogsByCategory`, `relatedBlogs`
- **Category**: `categories`, `categoryDetail`, `rootCategories`, `childCategories`
- **Comment**: `commentsByTarget`, `commentCount`, `adminComments`
- **User**: `adminUsers`, `adminUserDetails`, `adminEmployees`
- **Coupon**: `activeCoupons`, `couponStatisticsCache`, `pointsStatisticsCache`
- **Zone**: `districts`, `wardsByDistrict`
- **Khác**: `restaurantInfo`, `feedbackMediaList`, `feedbackMediaDetail`

### Cách triển khai Cache

**1. Thêm cache constant vào `CacheConfig.java`:**
```java
public static final String MY_CACHE = "myCache";
cacheConfigurations.put(MY_CACHE, defaultConfig.entryTtl(TTL_DEFAULT));
```

**2. Thêm @Cacheable cho GET methods (trong ServiceImpl):**
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

**4. DTO phải implement Serializable (nếu cache):**
```java
public class MyResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
```

---

## 8. Quy tắc Code

### Cấu trúc & Convention
- Java 17, sử dụng Lombok (@Getter, @Setter, @Builder, @RequiredArgsConstructor)
- Import đặt ở **đầu file**
- Endpoint RESTful theo convention: `/api/v1/{role}/{resource}`
- Sử dụng `@Valid` cho validation DTO
- Comment rõ ràng cho logic phức tạp (comment bằng tiếng Việt)
- Phân quyền: `@PreAuthorize`, `@RequireStaff`, `@RequireAdmin`
- Swagger annotations: `@Tag`, `@Operation`, `@ApiResponse` cho mỗi endpoint

### Protected Data
- Một số entity có field `isProtected = true` (User, Food, Blog)
- Chỉ SUPER_ADMIN mới có quyền sửa/xóa dữ liệu được bảo vệ
- Sử dụng `@RequireSuperAdmin` hoặc check trong service

### Bảo mật
- Không tự ý sửa file `.env`
- Kiểm tra xác thực/phân quyền trước thao tác nhạy cảm
- Sử dụng biến môi trường cho thông tin bảo mật (JWT_SECRET, API keys...)
- Password validation: Custom `@ValidPassword` annotation

---

## 9. Infrastructure

### Docker Compose
```
Services:
├── mysql       (MySQL 8.0, port 3306)
├── redis       (Redis 7 Alpine, port 6379)
└── backend     (Spring Boot, port 8080)
```

- **RAM**: Backend giới hạn `-Xms256m -Xmx400m` (chạy trên Lightsail nhỏ)
- **Docker image**: `duylinhgct/foodorder-backend:latest`

### Profiles
| Profile | File | Mô tả |
|---------|------|-------|
| `local` | `application-local.yml` | Chạy local, kết nối MySQL/Redis localhost |
| `docker` | `application-docker.yml` | Chạy trong Docker, kết nối qua container name |

### Environment Variables cần thiết
```
# Database
MYSQL_DATABASE_URL, MYSQL_USERNAME, MYSQL_PASSWORD

# JWT
JWT_SECRET, JWT_EXPIRATION

# AWS S3
AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_REGION

# Email
BREVO_API_KEY, BREVO_SENDER_EMAIL

# OAuth2
GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET
FACEBOOK_APP_ID, FACEBOOK_APP_SECRET

# Payment
ZALOPAY_APP_ID, ZALOPAY_KEY1, ZALOPAY_KEY2, ZALOPAY_CALLBACK_URL, ZALOPAY_REDIRECT_URL

# AI Chatbot
OPENAI_API_KEY

# Search
ALGOLIA_APPLICATION_ID, ALGOLIA_API_KEY, ALGOLIA_INDEX_NAME

# App
RESET_PASSWORD_URL, OAUTH2_REDIRECT_URL, ADMIN_EMAIL, STORE_NAME
```

---

## 10. Database Schema (Entities tổng hợp)

### Bảng chính
| Bảng | Entity | Mô tả |
|------|--------|-------|
| `users` | User | Người dùng |
| `roles` | Role | Vai trò (ROLE_USER, ROLE_STAFF, ROLE_ADMIN) |
| `user_tokens` | UserToken | Token xác thực email, reset password |
| `foods` | Food | Món ăn |
| `food_images` | FoodImage | Ảnh món ăn |
| `food_variants` | FoodVariant | Biến thể (size, topping) |
| `categories` | Category | Danh mục (cây phân cấp) |
| `cart_items` | CartItem | Giỏ hàng |
| `orders` | Order | Đơn hàng |
| `order_items` | OrderItem | Chi tiết đơn hàng |
| `order_tracking` | OrderTracking | Lịch sử trạng thái đơn |
| `blogs` | Blog | Bài viết |
| `blog_categories` | BlogCategory | Danh mục blog |
| `comments` | Comment | Bình luận (polymorphic) |
| `likes` | Like | Lượt thích (polymorphic) |
| `shares` | Share | Lượt chia sẻ |
| `favorite_foods` | FavoriteFood | Món yêu thích |
| `coupons` | Coupon | Mã giảm giá |
| `coupon_usage` | CouponUsage | Lịch sử sử dụng coupon |
| `coupon_categories` | - | Coupon ↔ Category (ManyToMany) |
| `coupon_foods` | - | Coupon ↔ Food (ManyToMany) |
| `coupon_users` | - | Coupon ↔ User (ManyToMany) |
| `reward_points` | RewardPoint | Số dư điểm thưởng |
| `point_history` | PointHistory | Lịch sử điểm |
| `point_rules` | PointRule | Quy tắc tích điểm |
| `notifications` | Notification | Thông báo |
| `user_staff_conversations` | Conversation | Cuộc hội thoại chat |
| `chat_messages` | ChatMessage | Tin nhắn chat |
| `chatbot_messages` | ChatbotMessage | Tin nhắn chatbot AI |
| `knowledge_base` | KnowledgeBase | Knowledge base cho RAG |
| `contact_messages` | ContactMessage | Tin nhắn liên hệ |
| `restaurant_info` | RestaurantInfo | Thông tin nhà hàng |
| `restaurant_galleries` | RestaurantGallery | Gallery nhà hàng |
| `feedback_media` | FeedbackMedia | Feedback ảnh/video |
| `districts` | District | Quận/huyện |
| `wards` | Ward | Phường/xã |

---

## 11. Checklist khi tạo API mới

- [ ] Tách đúng Controller → Service → ServiceImpl → Repository
- [ ] Đặt đúng prefix endpoint: `/api/v1/{public|client|staff|admin}/{resource}`
- [ ] Sử dụng DTO cho request/response (implement `Serializable` nếu cần cache)
- [ ] Validate với `@Valid`
- [ ] Phân quyền phù hợp (`@RequireStaff`, `@RequireAdmin`, hoặc trong SecurityConfig)
- [ ] Error trả về `errorCode` chuẩn (qua `BadRequestException`, `ResourceNotFoundException`...)
- [ ] Swagger annotations (`@Tag`, `@Operation`, `@ApiResponse`)
- [ ] **Xem xét thêm Cache** cho GET APIs (thêm constant + TTL trong `CacheConfig.java`)
- [ ] **CacheEvict** cho CUD methods
- [ ] Cập nhật tài liệu trong `/docs`

---

## 12. Tài liệu đã có (`/docs`)

| File | Mô tả |
|------|-------|
| `ADMIN_COMMENT_API.md` | API quản lý comment (Admin) |
| `ALGOLIA_SEARCH_API.md` | Tích hợp Algolia Search |
| `BLOG_API.md` | API Blog/Tin tức |
| `BLOG_CONTENT_TYPES_API.md` | Các loại nội dung blog |
| `BUSINESS_HOURS_API.md` | Giờ hoạt động nhà hàng |
| `CACHE_OPTIMIZATION.md` | Chiến lược tối ưu cache |
| `CONTACT_API.md` | API liên hệ |
| `DOCKER_README.md` | Hướng dẫn Docker |
| `ENDPOINT_MIGRATION.md` | Lịch sử chuyển đổi endpoint convention |
| `FACEBOOK_OAUTH2_FLOW.md` | Flow đăng nhập Facebook |
| `FOOD_CACHE_API.md` | Cache cho Food module |
| `GOOGLE_OAUTH2_FLOW.md` | Flow đăng nhập Google |
| `LIKE_SHARE_API.md` | API Like/Share |
| `ORDER_STATUS_EMAIL_API.md` | Email thông báo đơn hàng |
| `PROTECTED_DATA.md` | Dữ liệu được bảo vệ |
| `REDIS_CACHE_API.md` | Tổng quan Redis Cache |
| `RESTAURANT_INFO_API.md` | API thông tin nhà hàng |

---

## 13. Gợi ý mở rộng tương lai

| Tính năng | Mô tả | Mức độ |
|----------|-------|--------|
| **Review & Rating** | Cho phép đánh giá sao cho món ăn, blog | Trung bình |
| **Notification Push** | Firebase Cloud Messaging cho mobile | Trung bình |
| **Inventory Management** | Quản lý kho nguyên liệu | Cao |
| **Multi-branch** | Hỗ trợ nhiều chi nhánh nhà hàng | Cao |
| **Loyalty Tier** | Hạng thành viên (Bronze/Silver/Gold) dựa trên điểm | Trung bình |
| **Scheduled Orders** | Đặt hàng trước theo lịch | Trung bình |
| **Delivery Tracking** | Theo dõi vị trí giao hàng real-time (GPS) | Cao |
| **Analytics Export** | Xuất báo cáo PDF/Excel | Thấp |
| **Multi-language** | Hỗ trợ đa ngôn ngữ (i18n) | Trung bình |
| **Unit Tests** | Bổ sung test coverage cho các service | Trung bình |
| **Rate Limiting** | Giới hạn request tại API Gateway level | Thấp |
| **VNPay Integration** | Hoàn thiện tích hợp VNPay | Thấp |

