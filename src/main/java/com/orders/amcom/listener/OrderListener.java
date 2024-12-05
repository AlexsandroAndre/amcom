package com.orders.amcom.listener;

import com.orders.amcom.model.Order;
import com.orders.amcom.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.orders.amcom.config.RabbitMQConfig.QUEUE_NAME;

@RequiredArgsConstructor
@Component
public class OrderListener {

    private final OrderService orderService;

    @RabbitListener(queues = QUEUE_NAME, concurrency = "5-10")
    public void processMessage(Order order){
        orderService.createOrder(order);
    }
}
