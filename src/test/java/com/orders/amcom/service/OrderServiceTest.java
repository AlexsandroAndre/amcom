package com.orders.amcom.service;

import com.orders.amcom.dto.OrderDto;
import com.orders.amcom.enums.OrderStatus;
import com.orders.amcom.exception.OrderException;
import com.orders.amcom.model.Order;
import com.orders.amcom.model.Product;
import com.orders.amcom.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.orders.amcom.service.OrderService.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_ShouldSaveOrderWithCalculatedTotalAmount() {

        Product product1 = new Product();
        product1.setPrice(BigDecimal.valueOf(50));
        product1.setQuantity(2);

        Product product2 = new Product();
        product2.setPrice(BigDecimal.valueOf(100));
        product2.setQuantity(1);

        List<Product> products = List.of(product1, product2);

        Order order = new Order();
        order.setProducts(products);

        Order savedOrder = new Order();
        savedOrder.setId(UUID.randomUUID().toString());
        savedOrder.setTotalAmount(BigDecimal.valueOf(200));
        savedOrder.setExternalId("ORD-123");
        savedOrder.setProducts(products);
        savedOrder.setStatus(OrderStatus.PENDING);

        Mockito.when(orderRepository.save(Mockito.any(Order.class))).thenReturn(savedOrder);

        Order result = orderService.createOrder(order);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(200), result.getTotalAmount());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals("ORD-123", result.getExternalId());
        assertEquals(products, result.getProducts());
        Mockito.verify(orderRepository, Mockito.times(1)).save(Mockito.any(Order.class));
    }

    @Test
    void createOrder_ShouldThrowException_WhenOrderHasNoProducts() {
        Order order = new Order();
        order.setProducts(Collections.emptyList()); // Nenhum produto

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(order);
        });

        assertEquals("The order must contain at least one product.", exception.getMessage());
        Mockito.verifyNoInteractions(orderRepository);
    }

    @Test
    void updateOrderStatus_ShouldUpdateStatus_WhenValidData() {

        String externalId = "ORD-123";
        OrderDto orderDto = new OrderDto();
        orderDto.setId(UUID.randomUUID().toString());
        orderDto.setStatus(OrderStatus.COMPLETED);

        Order existingOrder = new Order();
        existingOrder.setId(orderDto.getId());
        existingOrder.setExternalId(externalId);
        existingOrder.setStatus(OrderStatus.PENDING);

        Mockito.when(orderRepository.findOrderByIdAndExternalId(orderDto.getId(), externalId))
                .thenReturn(Optional.of(existingOrder));
        Mockito.when(orderRepository.save(Mockito.any(Order.class)))
                .thenReturn(existingOrder);

        Order updatedOrder = orderService.updateOrderStatus(externalId, orderDto);

        assertNotNull(updatedOrder);
        assertEquals(OrderStatus.COMPLETED, updatedOrder.getStatus());
        Mockito.verify(orderRepository).findOrderByIdAndExternalId(orderDto.getId(), externalId);
        Mockito.verify(orderRepository).save(existingOrder);
    }

    @Test
    void updateOrderStatus_ShouldThrowException_WhenOrderNotFound() {

        String externalId = "ORD-999";
        OrderDto orderDto = new OrderDto();
        orderDto.setId(UUID.randomUUID().toString());
        orderDto.setStatus(OrderStatus.COMPLETED);

        Mockito.when(orderRepository.findOrderByIdAndExternalId(orderDto.getId(), externalId))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(OrderException.class, () -> {
            orderService.updateOrderStatus(externalId, orderDto);
        });

        assertEquals(String.format(ORDER_NOT_FOUND, orderDto.getId()) , exception.getMessage());
        Mockito.verify(orderRepository).findOrderByIdAndExternalId(orderDto.getId(), externalId);
        Mockito.verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void updateOrderStatus_ShouldThrowException_WhenCurrentStatusNotPending() {

        String externalId = "ORD-123";
        OrderDto orderDto = new OrderDto();
        orderDto.setId(UUID.randomUUID().toString());
        orderDto.setStatus(OrderStatus.COMPLETED);

        Order existingOrder = new Order();
        existingOrder.setId(orderDto.getId());
        existingOrder.setExternalId(externalId);
        existingOrder.setStatus(OrderStatus.COMPLETED);

        Mockito.when(orderRepository.findOrderByIdAndExternalId(orderDto.getId(), externalId))
                .thenReturn(Optional.of(existingOrder));

        Exception exception = assertThrows(OrderException.class, () -> {
            orderService.updateOrderStatus(externalId, orderDto);
        });

        assertEquals(ONLY_PENDING_ORDERS_CAN_BE_MARKED_AS_COMPLETED, exception.getMessage());
        Mockito.verify(orderRepository).findOrderByIdAndExternalId(orderDto.getId(), externalId);
        Mockito.verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void updateOrderStatus_ShouldThrowException_WhenSettingStatusToPending() {

        String externalId = "ORD-123";
        OrderDto orderDto = new OrderDto();
        orderDto.setId(UUID.randomUUID().toString());
        orderDto.setStatus(OrderStatus.PENDING);

        Order existingOrder = new Order();
        existingOrder.setId(orderDto.getId());
        existingOrder.setExternalId(externalId);
        existingOrder.setStatus(OrderStatus.PENDING);

        Mockito.when(orderRepository.findOrderByIdAndExternalId(orderDto.getId(), externalId))
                .thenReturn(Optional.of(existingOrder));

        Exception exception = assertThrows(OrderException.class, () -> {
            orderService.updateOrderStatus(externalId, orderDto);
        });

        assertEquals(IT_IS_NOT_POSSIBLE_TO_CHANGE_TO_PENDING, exception.getMessage());
        Mockito.verify(orderRepository).findOrderByIdAndExternalId(orderDto.getId(), externalId);
        Mockito.verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void getAllOrders_ShouldReturnOrders_WhenFiltersAreApplied() {

        OrderStatus status = OrderStatus.PENDING;
        LocalDate startDate = LocalDate.of(2023, 12, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        Pageable pageable = PageRequest.of(0, 10);

        List<Order> orders = new ArrayList<>();

        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setExternalId("ORD-123");
        order.setStatus(OrderStatus.PENDING);
        order.setProducts(new ArrayList<>());
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.setCreatedAt(LocalDateTime.now());
        orders.add(order);

        Order order2 = new Order();
        order2.setId(UUID.randomUUID().toString());
        order2.setExternalId("ORD-124");
        order2.setStatus(OrderStatus.PENDING);
        order2.setProducts(new ArrayList<>());
        order2.setTotalAmount(BigDecimal.valueOf(200));
        order2.setCreatedAt(LocalDateTime.now());
        orders.add(order2);


        Page<Order> pagedOrders = new PageImpl<>(orders);

        Mockito.when(orderRepository.findAll(Mockito.any(Specification.class), Mockito.eq(pageable)))
                .thenReturn(pagedOrders);

        Page<Order> result = orderService.getAllOrders(status, startDate, endDate, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(OrderStatus.PENDING, result.getContent().get(0).getStatus());

        Mockito.verify(orderRepository).findAll(Mockito.any(Specification.class), Mockito.eq(pageable));
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders_WhenNoFiltersApplied() {

        OrderStatus status = null;
        LocalDate startDate = null;
        LocalDate endDate = null;
        Pageable pageable = PageRequest.of(0, 10);

        List<Order> orders = new ArrayList<>();

        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setExternalId("ORD-123");
        order.setStatus(OrderStatus.PENDING);
        order.setProducts(new ArrayList<>());
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.setCreatedAt(LocalDateTime.now());
        orders.add(order);

        Order order2 = new Order();
        order2.setId(UUID.randomUUID().toString());
        order2.setExternalId("ORD-124");
        order2.setStatus(OrderStatus.COMPLETED);
        order2.setProducts(new ArrayList<>());
        order2.setTotalAmount(BigDecimal.valueOf(200));
        order2.setCreatedAt(LocalDateTime.now());
        orders.add(order2);

        Page<Order> pagedOrders = new PageImpl<>(orders);

        Mockito.when(orderRepository.findAll(Mockito.any(Specification.class), Mockito.eq(pageable)))
                .thenReturn(pagedOrders);

        Page<Order> result = orderService.getAllOrders(status, startDate, endDate, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        Mockito.verify(orderRepository).findAll(Mockito.any(Specification.class), Mockito.eq(pageable));
    }

    @Test
    void getAllOrders_ShouldReturnEmptyPage_WhenNoOrdersMatchFilters() {
        OrderStatus status = OrderStatus.PENDING;
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);
        Pageable pageable = PageRequest.of(0, 10);

        Page<Order> emptyPage = Page.empty(pageable);

        Mockito.when(orderRepository.findAll(Mockito.any(Specification.class), Mockito.eq(pageable)))
                .thenReturn(emptyPage);

        Page<Order> result = orderService.getAllOrders(status, startDate, endDate, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        Mockito.verify(orderRepository).findAll(Mockito.any(Specification.class), Mockito.eq(pageable));
    }

}
