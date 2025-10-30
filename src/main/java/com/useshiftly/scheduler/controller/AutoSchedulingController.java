package com.useshiftly.scheduler.controller;

import com.useshiftly.scheduler.dto.AutoScheduleRequestDTO;
import com.useshiftly.scheduler.dto.AutoScheduleResultDTO;
import com.useshiftly.scheduler.service.AutoSchedulingService;
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
