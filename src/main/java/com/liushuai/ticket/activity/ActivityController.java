package com.liushuai.ticket.activity;

import com.liushuai.ticket.common.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/activities")
public class ActivityController {
    private final ActivityService service;
    public ActivityController(ActivityService service) { this.service = service; }
    @GetMapping("/{id}") public ApiResponse<Activity> detail(@PathVariable long id) { return ApiResponse.ok(service.get(id)); }
    @GetMapping("/{id}/stock") public ApiResponse<Map<String, Integer>> stock(@PathVariable long id) {
        return ApiResponse.ok(Map.of("availableStock", service.getStock(id)));
    }
}
