package com.orders.amcom.dto;

import com.orders.amcom.model.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ProductDto {
    private String name;
    private BigDecimal price;
    private Integer quantity;

    public static ProductDto fromEntity(Product product) {
        ProductDto dto = new ProductDto();
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setQuantity(product.getQuantity());
        return dto;
    }

    public static Product fromDTO(ProductDto dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        return product;
    }

    public Product toEntity() {
        Product product = new Product();
        product.setName(this.name);
        product.setPrice(this.price);
        product.setQuantity(this.quantity);
        return product;
    }

    public static List<ProductDto> fromEntityList(List<Product> products) {
        return products.stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());
    }

    public static List<Product> toEntityList(List<ProductDto> dtos) {
        return dtos.stream()
                .map(ProductDto::toEntity)
                .collect(Collectors.toList());
    }
}
