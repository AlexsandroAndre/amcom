package com.orders.amcom.service;

import com.orders.amcom.enums.OrderStatus;
import com.orders.amcom.model.Order;
import com.orders.amcom.model.Product;
import com.orders.amcom.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductIntegrationService {
    private final OrderRepository orderRepository;

    public List<Product> fetchProductsFromServiceA() {
        //todo: melhor maneira acredito seria por Feign Client, mas como é dados simulados fiz de forma mockada.
        return new ArrayList<>();
    }

    public List<Product> fetchProductsFromServiceB() {
        List<Order> completedOrders = orderRepository.findByStatus(OrderStatus.COMPLETED.toString());

        return completedOrders.stream()
                .flatMap(order -> order.getProducts().stream())
                .collect(Collectors.toList());
    }
}
