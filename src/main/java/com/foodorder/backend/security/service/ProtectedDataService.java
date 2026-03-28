package com.foodorder.backend.security.service;

import com.foodorder.backend.exception.ForbiddenException;
import com.foodorder.backend.security.CustomUserDetails;
import com.foodorder.backend.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service dùng chung cho việc kiểm tra quyền trên dữ liệu được bảo vệ (isProtected = true)
 * Tập trung logic kiểm tra SUPER_ADMIN, tránh code trùng lặp ở các ServiceImpl
 *
 * Sử dụng: Inject vào các ServiceImpl cần kiểm tra isProtected
 */
@Service
@Slf4j
public class ProtectedDataService {

    /**
     * Lấy thông tin user hiện tại từ SecurityContext
     *
     * @return User hiện tại hoặc null nếu chưa đăng nhập
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUser();
        }
        return null;
    }

    /**
     * Kiểm tra user hiện tại có phải là SUPER_ADMIN không
     *
     * @return true nếu user hiện tại có role SUPER_ADMIN
     */
    public boolean isCurrentUserSuperAdmin() {
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.isSuperAdmin();
    }

    /**
     * Kiểm tra quyền thao tác trên dữ liệu được bảo vệ
     * Nếu dữ liệu được bảo vệ (isProtected = true) và user không phải SUPER_ADMIN → throw ForbiddenException
     *
     * @param isProtected trạng thái bảo vệ của dữ liệu
     * @param action      hành động đang thực hiện (ví dụ: "cập nhật", "xóa", "cập nhật trạng thái")
     */
    public void checkPermission(Boolean isProtected, String action) {
        if (Boolean.TRUE.equals(isProtected) && !isCurrentUserSuperAdmin()) {
            log.warn("User does not have permission to {} protected data", action);
            throw new ForbiddenException(
                    "Protected data, only Super Admin can " + action,
                    "PROTECTED_DATA_ACCESS_DENIED"
            );
        }
    }

    /**
     * Overload cho kiểu boolean primitive
     *
     * @param isProtected trạng thái bảo vệ của dữ liệu
     * @param action      hành động đang thực hiện
     */
    public void checkPermission(boolean isProtected, String action) {
        checkPermission(Boolean.valueOf(isProtected), action);
    }
}

