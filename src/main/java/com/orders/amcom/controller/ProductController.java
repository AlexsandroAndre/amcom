package com.orders.amcom.controller;

import com.orders.amcom.model.Product;
import com.orders.amcom.service.ProductIntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductIntegrationService productIntegrationService;

    public ProductController(ProductIntegrationService productIntegrationService) {
        this.productIntegrationService = productIntegrationService;
    }

    @GetMapping("/service-a")
    public ResponseEntity<List<Product>> getProductsFromServiceA() {
        //todo: melhor maneira acredito seria por Feign Client, mas como é dados simulados fiz de forma mockada.
        return ResponseEntity.ok(productIntegrationService.fetchProductsFromServiceA());
    }

    @GetMapping("/service-b")
    public ResponseEntity<List<Product>> getProductsFromServiceB() {
        return ResponseEntity.ok(productIntegrationService.fetchProductsFromServiceB());
    }
}
