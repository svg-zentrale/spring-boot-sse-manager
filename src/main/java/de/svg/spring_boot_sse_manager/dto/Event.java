package de.svg.spring_boot_sse_manager.dto;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

/**
 * event for SSEManager.
 */
public class Event {
    @Setter
    private SseEventBuilder builder;

    @Getter
    @Setter
    private Object payload;

    @Getter
    @Setter
    private EventType eventName;

    @Getter
    @Setter
    private Date date;

    public Event(final EventType eventName) {
        buildEvent(eventName, "");
    }

    public Event(final EventType eventName, final Object payload) {
        buildEvent(eventName, payload);
    }

    public SseEmitter.SseEventBuilder get() {
        return builder;
    }

    private void buildEvent(final EventType eventName, final Object payload) {
        setPayload(payload);
        setEventName(eventName);
        setDate(new Date());
        setBuilder(SseEmitter
                .event()
                .name(getEventName().toString())
                .comment(getDate().toString())
                .data(getPayload(), MediaType.APPLICATION_JSON));
    }
}
