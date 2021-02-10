package de.svg.spring_boot_sse_manager;

import de.svg.spring_boot_sse_manager.dto.ErrorPayload;
import de.svg.spring_boot_sse_manager.dto.Event;
import de.svg.spring_boot_sse_manager.dto.EventMessagePayload;
import de.svg.spring_boot_sse_manager.dto.EventType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * SSE stream.
 */
@Slf4j
public class SSEStream extends SseEmitter {
    private static final int MAX_INFO_LENGTH = 30;
    private static final int MAX_DEBUG_LENGTH = 255;
    private static final String MESSAGE_TO_LONG_ERROR_MESSAGE = "message is too long, max size is ";
    private Future<?> callbackFuture;
    private boolean timeoutTriggered;

    private Integer runningId = 0;
    private Timer timer;
    private AsyncTaskExecutor taskExecutor;

    public SSEStream(final Consumer<SSEStream> callback) {
        super();
        onTimeout(this::handleTimeout);
        onCompletion(this::cancelHeartbeat);
        run(callback);
    }

    public SSEStream(final Consumer<SSEStream> callback, final Long timeout) {
        super(timeout);
        onTimeout(this::handleTimeout);
        onCompletion(this::cancelHeartbeat);
        run(callback);
    }


    public SSEStream(final Consumer<SSEStream> callback, final AsyncTaskExecutor taskExecutor) {
        super();
        this.taskExecutor = taskExecutor;
        onTimeout(this::handleTimeout);
        onCompletion(this::cancelHeartbeat);
        run(callback);
    }

    public SSEStream(final Consumer<SSEStream> callback, final Long timeout, final AsyncTaskExecutor taskExecutor) {
        super(timeout);
        this.taskExecutor = taskExecutor;
        onTimeout(this::handleTimeout);
        onCompletion(this::cancelHeartbeat);
        run(callback);
    }

    public void debug(final String message) {
        if (message.length() < MAX_DEBUG_LENGTH) {
            sendEvent(EventType.DEBUG, message);
        } else {
            throw new InvalidParameterException(MESSAGE_TO_LONG_ERROR_MESSAGE + MAX_DEBUG_LENGTH);
        }
    }

    public void debug(final Object message) {
        sendEvent(EventType.DEBUG, message);
    }

    public void info(final String message) {
        if (message.length() > MAX_INFO_LENGTH) {
            log.warn("info call only allows messages up to " + MAX_INFO_LENGTH + " chars and abbreviates the rest.");
        }
        sendEvent(EventType.INFO, StringUtils.abbreviate(message, MAX_INFO_LENGTH));
    }

    public void info(final Object message) {
        sendEvent(EventType.INFO, message);
    }

    public void done(final Object result) {
        log.debug("send done event with object: " + result.toString());
        sendEvent(new Event(EventType.DONE, result).get());
        cancelHeartbeat();
        complete();
    }

    public void error(final Integer status, final Throwable error) {
        final String message = error.getMessage();
        log.error("send error event with message: " + message);
        final ErrorPayload payload = ErrorPayload.builder().status(status).details(message).build();
        sendEvent(new Event(EventType.ERROR, payload).get());
        cancelHeartbeat();
        completeWithError(error);
    }

    private void sendEvent(final EventType type, final String message) {
        log.debug(type.toString() + " event with message: " + message);
        sendEvent(new Event(type, EventMessagePayload.builder().message(message).build()).get());
    }

    private void sendEvent(final EventType type, final Object payload) {
        log.debug(type.toString() + " event with payload: " + payload.toString());
        sendEvent(new Event(type, payload).get());
    }

    public void sendEvent(final SseEmitter.SseEventBuilder builder) {
        synchronized (this) {
            try {
                send(builder.id((++runningId).toString()));
            } catch (IOException ex) {
                log.error("IO Exception" + ex.getMessage());
            }
        }
    }


    private void run(final Consumer<SSEStream> callback) {
        startHeartBeat();
        if (this.taskExecutor != null) {
            callbackFuture = this.taskExecutor.submit(() -> {
                try {
                    callback.accept(this);
                } catch (Throwable throwable) {
                    if (!timeoutTriggered) {
                        error(500, throwable);
                    }
                }
            });
        } else {
            callbackFuture = Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    callback.accept(this);
                } catch (Throwable throwable) {
                    if (!timeoutTriggered) {
                        error(500, throwable);
                    }
                }
            });
        }
    }

    /**
     * starts heartbeat events every 15 sec to keep sse connection alive.
     */
    private void startHeartBeat() {
        final TimerTask task = new TimerTask() {

            @Override
            public void run() {
                sendEvent(new Event(EventType.HEARTBEAT).get());
            }
        };

        timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, 15_000);
    }

    private void cancelHeartbeat() {
        synchronized (this) {
            timer.cancel();
        }
    }

    private void handleTimeout() {
        timeoutTriggered = true;
        log.error("timeout @ " + getTimeout());
        callbackFuture.cancel(true);
    }

}
