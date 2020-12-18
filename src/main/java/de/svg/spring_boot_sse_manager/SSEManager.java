package de.svg.spring_boot_sse_manager;

import de.svg.spring_boot_sse_manager.dto.ErrorPayload;
import de.svg.spring_boot_sse_manager.dto.Event;
import de.svg.spring_boot_sse_manager.dto.EventType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * SSEManager.
 */
@Slf4j
public class SSEManager {
    private static final int MAX_INFO_LENGTH = 30;
    private static final int MAX_DEBUG_LENGTH = 255;
    private static final String MESSAGE_TO_LONG_ERROR_MESSAGE = "message is too long, max size is ";

    @Getter
    private final SseEmitter emitter;
    private Integer runningId = 0;
    private Timer timer;

    public SSEManager() {
        emitter = new SseEmitter();
        startHeartBeat();
    }

    public SSEManager(final Long timeout) {
        emitter = new SseEmitter(timeout);
        startHeartBeat();
    }

    public void debug(final String message) {

        if (message.length() < MAX_DEBUG_LENGTH) {
            log.debug("send debug event with message: " + message);
            send(new Event(EventType.DEBUG, message).get());
        } else {
            throw new InvalidParameterException(MESSAGE_TO_LONG_ERROR_MESSAGE + MAX_DEBUG_LENGTH);
        }
    }

    public void info(final String message) {

        if (message.length() < MAX_INFO_LENGTH) {
            log.debug("send info event with message: " + message);
            send(new Event(EventType.INFO, message).get());
        } else {
            throw new InvalidParameterException(MESSAGE_TO_LONG_ERROR_MESSAGE + MAX_INFO_LENGTH);
        }
    }

    public void info() {
        log.debug("send info event");
        send(new Event(EventType.INFO).get());
    }

    public void done(final Object result) {
        log.debug("send done event with object: " + result.toString());
        send(new Event(EventType.DONE, result).get());
        cancelHeartbeat();
        emitter.complete();
    }

    public void error(final Integer status, final Throwable error) {
        final String message = error.getMessage();
        log.error("send error event with message: " + message);
        final ErrorPayload payload = ErrorPayload.builder().status(status).details(message).build();
        send(new Event(EventType.ERROR, payload).get());
        cancelHeartbeat();
        emitter.completeWithError(error);
    }


    public void send(final SseEmitter.SseEventBuilder builder) {
        synchronized (this) {
            try {
                emitter.send(builder.id((++runningId).toString()));
            } catch (IOException ex) {
                log.error(ex.getMessage());
            }
        }
    }

    /**
     * starts heartbeat events every 15 sec to keep sse connection alive.
     */
    private void startHeartBeat() {
        final TimerTask task = new TimerTask() {

            @Override
            public void run() {
                send(new Event(EventType.HEARTBEAT).get());
            }
        };

        timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, 15_000);
    }

    public void cancelHeartbeat() {
        synchronized (this) {
            timer.cancel();
        }
    }

}
