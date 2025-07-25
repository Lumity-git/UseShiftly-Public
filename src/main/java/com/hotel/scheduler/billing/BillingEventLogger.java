package com.hotel.scheduler.billing;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BillingEventLogger {
    private final List<Map<String, Object>> events = new ArrayList<>();

    public void logEvent(String type, String adminEmail, Map<String, Object> details) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", type);
        event.put("adminEmail", adminEmail);
        event.put("details", details);
        event.put("timestamp", LocalDateTime.now());
        events.add(event);
    }

    public List<Map<String, Object>> getAllEvents() {
        return events;
    }

    public List<Map<String, Object>> getEventsForAdmin(String adminEmail) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> e : events) {
            if (e.get("adminEmail").equals(adminEmail)) {
                result.add(e);
            }
        }
        return result;
    }
}
