package com.orders.amcom.service;

import com.orders.amcom.model.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class OrderQueueService {
    private final BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>();
    private final OrderService orderService;

    public OrderQueueService(OrderService orderService) {
        this.orderService = orderService;
        startWorker();
    }

    public void addToQueue(Order order) {
        orderQueue.add(order); // Adiciona o pedido à fila
    }

    private void startWorker() {
        new Thread(() -> {
            while (true) {
                try {
                    Order order = orderQueue.take(); // Remove o pedido da fila para processamento
                    orderService.processOrder(order);
                } catch (Exception e) {
                    e.printStackTrace(); // Log de erros
                }
            }
        }).start();
    }
}

