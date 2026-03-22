package com.foodorder.backend.user.repository;

import com.foodorder.backend.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository để truy vấn bảng roles
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Tìm role theo code (ROLE_USER, ROLE_STAFF, ROLE_ADMIN, ROLE_SUPER_ADMIN)
     */
    Optional<Role> findByCode(String code);

    /**
     * Kiểm tra role có tồn tại theo code không
     */
    boolean existsByCode(String code);
}
