package com.liushuai.ticket.order;

import com.liushuai.ticket.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ticket-orders")
public class OrderController {
    private final OrderService service;
    public OrderController(OrderService service) { this.service = service; }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TicketOrder> create(@Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.ok(service.create(request));
    }
}
