package de.svg.spring_boot_sse_manager.dto;


import lombok.Builder;
import lombok.Data;

/**
 * ErrorPayload.
 */
@Data
@Builder
public class EventMessagePayload {
    private String message;
}
