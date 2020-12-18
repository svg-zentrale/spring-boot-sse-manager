package de.svg.spring_boot_sse_manager.dto;

/**
 * EventType (known events in this lib).
 */
public enum EventType {
    HEARTBEAT,
    ERROR,
    DEBUG,
    INFO,
    DONE;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
