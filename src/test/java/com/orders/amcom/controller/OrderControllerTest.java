package com.orders.amcom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orders.amcom.dto.OrderDto;
import com.orders.amcom.enums.OrderStatus;
import com.orders.amcom.exception.OrderNotFoundException;
import com.orders.amcom.model.Order;
import com.orders.amcom.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllOrders_ShouldReturnOrders_WhenValidRequest() throws Exception {
        // Mocking the service
        Order mockOrder = new Order();
        mockOrder.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000").toString());
        mockOrder.setTotalAmount(BigDecimal.valueOf(100));
        mockOrder.setStatus(OrderStatus.COMPLETED);
        mockOrder.setExternalId("ORD-123");
        mockOrder.setProducts(new ArrayList<>());

        List<Order> mockOrders = new ArrayList<>();
        mockOrders.add(mockOrder);
        Page<Order> mockPage = new PageImpl<>(mockOrders);

        Mockito.when(orderService.getAllOrders(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(Pageable.class)
        )).thenReturn(mockPage);

        // Executing the GET request
        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].externalId").value("ORD-123"))
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"));
    }

    @Test
    void getAllOrders_ShouldReturnPaginatedResults() throws Exception {
        // Prepare mock data
        Order mockOrder = new Order();
        mockOrder.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000").toString());
        mockOrder.setTotalAmount(BigDecimal.valueOf(100));
        mockOrder.setStatus(OrderStatus.COMPLETED);
        mockOrder.setExternalId("ORD-123");
        mockOrder.setProducts(new ArrayList<>());

        List<Order> mockOrders = new ArrayList<>();
        mockOrders.add(mockOrder);
        Page<Order> mockPage = new PageImpl<>(mockOrders);

        // Mock service call with pagination
        Mockito.when(orderService.getAllOrders(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(mockPage);

        // Perform the GET request with pagination
        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].externalId").value("ORD-123"))
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"));
    }

    @Test
    void updateOrderStatus_ShouldReturnUpdatedOrder_WhenValidData() throws Exception {
        // Dados de entrada
        String externalId = "ORD-123";
        OrderDto orderDto = new OrderDto();
        orderDto.setStatus(OrderStatus.COMPLETED);

        // Dados de resposta esperada (mock)
        Order mockOrder = new Order();
        mockOrder.setExternalId(externalId);
        mockOrder.setStatus(OrderStatus.COMPLETED);
        mockOrder.setProducts(new ArrayList<>());

        // Criando o mock do serviço
        Mockito.when(orderService.updateOrderStatus(Mockito.eq(externalId), Mockito.any(OrderDto.class)))
                .thenReturn(mockOrder);

        // Realizando o teste de atualização de status
        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/{externalId}/status", externalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value(externalId))
                .andExpect(jsonPath("$.status").value(OrderStatus.COMPLETED.toString()));
    }

    @Test
    void updateOrderStatus_ShouldReturnNotFound_WhenOrderDoesNotExist() throws Exception {
        // Dados de entrada
        String externalId = "ORD-999";  // Um ID que não existe
        OrderDto orderDto = new OrderDto();
        orderDto.setStatus(OrderStatus.COMPLETED);

        // Mocking do serviço para lançar uma exceção quando o externalId não existir
        Mockito.when(orderService.updateOrderStatus(Mockito.eq(externalId), Mockito.any(OrderDto.class)))
                .thenThrow(new OrderNotFoundException("Order with externalId " + externalId + " not found"));

        // Realizando o teste de atualização de status
        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/{externalId}/status", externalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order with externalId ORD-999 not found"));
    }

    @Test
    void processOrder_ShouldReturnSuccessMessage_WhenOrderIsProcessed() throws Exception {
        // Criação de um pedido de teste
        Order order = new Order();
        order.setExternalId("ORD-123");
        order.setStatus(OrderStatus.COMPLETED);

        // Mocking do comportamento do serviço
        doNothing().when(orderService).processOrder(order);

        // Realizando a requisição e verificando a resposta
        mockMvc.perform(post("/api/orders/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(content().string("Pedido processado com sucesso."));

        verify(orderService, times(1)).processOrder(order);
    }

    @Test
    void processOrder_ShouldReturnErrorMessage_WhenExceptionIsThrown() throws Exception {
        // Criação de um pedido de teste
        Order order = new Order();
        order.setExternalId("ORD-123");
        order.setStatus(OrderStatus.COMPLETED);

        // Mocking do comportamento do serviço para lançar uma exceção
        doThrow(new RuntimeException("Erro de processamento")).when(orderService).processOrder(order);

        // Realizando a requisição e verificando a resposta
        mockMvc.perform(post("/api/orders/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Erro ao processar o pedido: Erro de processamento"));

        verify(orderService, times(1)).processOrder(order);
    }

    @Test
    void createOrder_ShouldReturnCreatedOrder_WhenValidData() throws Exception {
        // Criação de um pedido de entrada (DTO)
        OrderDto orderDto = new OrderDto();
        orderDto.setExternalId("ORD-123");
        orderDto.setStatus(OrderStatus.PENDING);
        orderDto.setProducts(new ArrayList<>());

        // Criação do pedido mockado
        Order savedOrder = new Order();
        savedOrder.setExternalId("ORD-123");
        savedOrder.setStatus(OrderStatus.PENDING);
        savedOrder.setProducts(new ArrayList<>());

        // Mocking do comportamento do serviço
        when(orderService.createOrder(any(Order.class))).thenReturn(savedOrder);

        // Realizando a requisição e verificando a resposta
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.externalId").value("ORD-123"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(orderService, times(1)).createOrder(any(Order.class));
    }

}
