package com.hotel.scheduler.controller;

import com.hotel.scheduler.dto.AutoScheduleRequestDTO;
import com.hotel.scheduler.dto.AutoScheduleResultDTO;
import com.hotel.scheduler.service.AutoSchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auto-scheduling")
public class AutoSchedulingController {
    private final AutoSchedulingService autoSchedulingService;

    @Autowired
    public AutoSchedulingController(AutoSchedulingService autoSchedulingService) {
        this.autoSchedulingService = autoSchedulingService;
    }

    @PostMapping
    public AutoScheduleResultDTO autoSchedule(@RequestBody AutoScheduleRequestDTO request) {
        return autoSchedulingService.autoSchedule(request);
    }
}
