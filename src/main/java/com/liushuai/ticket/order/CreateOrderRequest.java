package com.liushuai.ticket.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
        @NotNull Long activityId,
        @NotNull Long userId,
        @Min(1) @Max(2) Integer quantity,
        @NotBlank @Size(max = 64) String requestId) { }
