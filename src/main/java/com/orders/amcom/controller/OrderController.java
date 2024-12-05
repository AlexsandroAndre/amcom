package com.orders.amcom.controller;

import com.orders.amcom.dto.OrderDto;
import com.orders.amcom.enums.OrderStatus;
import com.orders.amcom.model.Order;
import com.orders.amcom.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderDto orderDto) {
        Order order = orderDto.fromDTO(orderDto);
        Order savedOrder  = orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderDto.fromEntity(savedOrder));
    }

    @PostMapping("/process")
    public ResponseEntity<String> processOrder(@RequestBody Order order) {
        try {
            orderService.processOrder(order);
            return ResponseEntity.ok("Pedido processado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao processar o pedido: " + e.getMessage());
        }
    }

    @PutMapping("/{externalId}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(@PathVariable String externalId, @RequestBody OrderDto dto) {
        Order savedOrder = orderService.updateOrderStatus(externalId, dto);
        return ResponseEntity.ok(OrderDto.fromEntity(savedOrder));
    }

    @GetMapping
    public ResponseEntity<Page<OrderDto>> getAllOrders(@RequestParam(value = "status", required = false) OrderStatus status,
        @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        Pageable pageable) {
        Page<Order> orders = orderService.getAllOrders(status, startDate, endDate, pageable);
        Page<OrderDto> orderDtos = orders.map(OrderDto::fromEntity);
        return ResponseEntity.ok(orderDtos);
    }
}
