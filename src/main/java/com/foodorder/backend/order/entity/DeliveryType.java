package com.foodorder.backend.order.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum định nghĩa các loại hình giao hàng
 */
@Getter
@AllArgsConstructor
public enum DeliveryType {
    DELIVERY("DELIVERY", "Giao hàng tận nơi"),
    TAKE_AWAY("TAKE_AWAY", "Nhận tại nhà hàng"),
    DINE_IN("DINE_IN", "Ăn tại nhà hàng");

    private final String code;
    private final String description;
}
