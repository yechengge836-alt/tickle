package com.liushuai.ticket.order;

import com.liushuai.ticket.auth.AuthService;
import com.liushuai.ticket.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ticket-orders")
public class OrderController {
    private final OrderService service;
    private final AuthService authService;
    public OrderController(OrderService service, AuthService authService) { this.service = service; this.authService = authService; }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TicketOrder> create(@RequestHeader(value = "X-Auth-Token", required = false) String accessToken,
                                             @Valid @RequestBody CreateOrderRequest request) {
        CreateOrderRequest authenticatedRequest = new CreateOrderRequest(request.activityId(), authService.requireUserId(accessToken), request.quantity(), request.requestId());
        return ApiResponse.ok(service.create(authenticatedRequest));
    }
}
