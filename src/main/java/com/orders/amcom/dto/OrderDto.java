package com.orders.amcom.dto;

import com.orders.amcom.enums.OrderStatus;
import com.orders.amcom.model.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderDto {
    private String id;
    private String externalId;
    private OrderStatus status;
    private List<ProductDto> products;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrderDto fromEntity(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setExternalId(order.getExternalId());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setProducts(order.getProducts().stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList()));
        return dto;
    }

    public static Order fromDTO(OrderDto dto) {
        Order order = new Order();
        order.setExternalId(dto.getExternalId());
        order.setStatus(dto.getStatus());
        order.setProducts(dto.getProducts().stream()
                .map(ProductDto::fromDTO)
                .collect(Collectors.toList()));
        order.setTotalAmount(dto.getTotalAmount());
        return order;
    }

    public Order toEntity() {
        Order order = new Order();
        order.setId(this.id);
        order.setExternalId(this.externalId);
        order.setStatus(this.status);
        order.setProducts(this.products.stream()
                .map(ProductDto::toEntity)
                .peek(product -> product.setOrder(order))
                .collect(Collectors.toList()));
        order.setTotalAmount(this.getTotalAmount());
        return order;
    }

    public static List<OrderDto> fromEntityList(List<Order> orders) {
        return orders.stream()
                .map(OrderDto::fromEntity)
                .collect(Collectors.toList());
    }

    public static List<Order> toEntityList(List<OrderDto> dtos) {
        return dtos.stream()
                .map(OrderDto::toEntity)
                .collect(Collectors.toList());
    }
}
