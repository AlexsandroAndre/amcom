package com.orders.amcom.service;

import com.orders.amcom.config.RabbitMQConfig;
import com.orders.amcom.dto.OrderDto;
import com.orders.amcom.enums.OrderStatus;
import com.orders.amcom.exception.OrderException;
import com.orders.amcom.model.Order;
import com.orders.amcom.model.Product;
import com.orders.amcom.repository.OrderRepository;
import com.orders.amcom.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final RabbitTemplate rabbitTemplate;

    public BigDecimal calculateOrderTotal(Order order) {
        return order.getProducts().stream()
                .map(product -> product.getPrice().multiply(BigDecimal.valueOf(product.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateStock(List<Product> products) {
        for (Product product : products) {
            Product storedProduct = productRepository.findById(product.getId())
                    .orElseThrow(() -> new OrderException("Produto não encontrado: " + product.getId()));
            if (storedProduct.getQuantity() < product.getQuantity()) {
                throw new OrderException("Estoque insuficiente para o produto: " + product.getName());
            }
            // Deduzir o estoque.
            storedProduct.setQuantity(storedProduct.getQuantity() - product.getQuantity());
            productRepository.save(storedProduct);
        }
    }

    private String generateExternalId() {
        return "ORD-" + System.currentTimeMillis();
    }

    public Order createOrder(Order order) {
         //validateStock(order.getProducts());

        BigDecimal totalAmount = calculateOrderTotal(order);
        order.setTotalAmount(totalAmount);

        List<Product> products = order.getProducts();
        products.forEach(product -> product.setOrder(order));

        order.setStatus(OrderStatus.PENDING);
        order.setExternalId(generateExternalId());

        Order savedOrder = orderRepository.save(order);

        return savedOrder;
    }

    @Transactional
    public Order updateOrderStatus(String externalId, OrderDto dto) {
        Order order = orderRepository.findOrderByIdAndExternalId(dto.getId(), externalId)
                .orElseThrow(() -> new OrderException("Pedido não encontrado: " + dto.getId()));

        if (!order.getStatus().equals(OrderStatus.PENDING) && dto.getStatus().equals(OrderStatus.COMPLETED)) {
            throw new OrderException("Somente pedidos PENDING podem ser marcados como COMPLETED.");
        }

        if (dto.getStatus().equals(OrderStatus.PENDING)) {
            throw new OrderException("Não é possível alterar para PENDING.");
        }

        order.setStatus(dto.getStatus());
        orderRepository.save(order);
        return order;
    }

    public Page<Order> getAllOrders(OrderStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return orderRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay()));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate.atTime(LocalTime.MAX)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }

    public void processOrder(Order order) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                order
        );
    }

}
