package de.svg.spring_boot_sse_manager;

import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class ParallelExecutionTest {

    private AsyncTaskExecutor asyncTaskExecutor;

    @Test
    public void multiAsyncTest() {
        new SSEStream((SSEStream stream) -> {
            List<Integer> collect = Stream.of(1, 2, 3)
                    .map(
                            integer ->
                                    (
                                            (Callable<Integer>) (
                                                    () -> {
                                                        stream.info("hi " + integer);
                                                        return integer;
                                                    }
                                            )
                                    )
                    )
                    .map(asyncTaskExecutor::submit)
                    .map(
                            future -> {
                                try {
                                    return future.get();
                                } catch (Exception e) {
                                    throw new IllegalStateException(e);
                                }
                            }
                    ).collect(Collectors.toList());
            stream.done(collect);

        }, 30000L);
    }
}
