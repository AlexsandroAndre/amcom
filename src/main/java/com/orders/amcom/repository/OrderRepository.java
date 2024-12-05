package com.orders.amcom.repository;

import com.orders.amcom.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {
    List<Order> findByStatus(String status);
    Optional<Order> findOrderByIdAndExternalId(String id, String externalId);
}
