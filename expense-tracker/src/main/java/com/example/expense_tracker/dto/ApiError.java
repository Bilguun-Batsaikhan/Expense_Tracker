package com.example.expense_tracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

@Value
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    String code;
    String message;
    int status;
    String correlationId;

    @Builder.Default
    Instant timestamp = Instant.now();

    // Lombok’s @Singular gives you both .detail(key, value) and .details(map)
    // support.
    @Singular("detail")
    Map<String, Object> details;

    // Ensure immutability (optional: override Lombok-generated getter)
    public Map<String, Object> getDetails() {
        if (details == null)
            return Collections.emptyMap();
        return Collections.unmodifiableMap(details);
    }
}
