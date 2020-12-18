package de.svg.spring_boot_sse_manager.dto;


import lombok.Builder;
import lombok.Data;

/**
 * ErrorPayload.
 */
@Data
@Builder
public class ErrorPayload {
    private int status;
    private String details;
}
