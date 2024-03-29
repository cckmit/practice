package top.hubby.threadpool;

import io.swagger.annotations.Api;
import jodd.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Collections;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * @author asd <br>
 * @create 2021-10-20 4:01 PM <br>
 * @project swagger-3 <br>
 */
@Slf4j
@Api(tags = "Pool")
@RestController
@RequestMapping("/pool/mix-use")
public class ThreadPoolMixuseController {

    /**
     * CallerRunsPolicy 慢的原因是如果进入queue了, 可能迟迟得不到执行;<br>
     * 如果没有进入queue 则执行还是很快的
     */
    private static ThreadPoolExecutor threadPool =
            new ThreadPoolExecutor(
                    2,
                    2,
                    1,
                    TimeUnit.HOURS,
                    new ArrayBlockingQueue<>(100),
                    new ThreadFactoryBuilder()
                            .setNameFormat("batchfileprocess-threadpool-%d")
                            .get(),
                    new ThreadPoolExecutor.CallerRunsPolicy());

    private static ThreadPoolExecutor asyncCalcThreadPool =
            new ThreadPoolExecutor(
                    200,
                    200,
                    1,
                    TimeUnit.HOURS,
                    new ArrayBlockingQueue<>(1000),
                    new ThreadFactoryBuilder().setNameFormat("asynccalc-threadpool-%d").get());

    private static ScheduledFuture<?> printStats(ThreadPoolExecutor threadPool) {
        return Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(
                        () -> {
                            log.info("=========================");
                            log.info("Pool Size: {}", threadPool.getPoolSize());
                            log.info("Active Threads: {}", threadPool.getActiveCount());
                            log.info(
                                    "Number of Tasks Completed: {}",
                                    threadPool.getCompletedTaskCount());
                            log.info("Number of Tasks in Queue: {}", threadPool.getQueue().size());
                            log.info("=========================");
                        },
                        0,
                        1,
                        TimeUnit.SECONDS);
    }

    private Callable<Integer> calcTask() {
        return () -> {
            TimeUnit.MILLISECONDS.sleep(10);
            return 1;
        };
    }

    @GetMapping("/wrong")
    public int wrong() throws ExecutionException, InterruptedException {
        return threadPool.submit(calcTask()).get();
    }

    @GetMapping("/right")
    public int right() throws ExecutionException, InterruptedException {
        return asyncCalcThreadPool.submit(calcTask()).get();
    }

    // @PostConstruct
    public void init() {
        printStats(threadPool);

        new Thread(
                        () -> {
                            String payload =
                                    IntStream.rangeClosed(1, 1_000_000)
                                            .mapToObj(__ -> "a")
                                            .collect(Collectors.joining(""));
                            while (true) {
                                threadPool.execute(
                                        () -> {
                                            try {

                                                TimeUnit.SECONDS.sleep(10);
                                                Files.write(
                                                        Paths.get("demo.txt"),
                                                        Collections.singletonList(
                                                                LocalTime.now().toString()
                                                                        + ":"
                                                                        + payload),
                                                        UTF_8,
                                                        CREATE,
                                                        TRUNCATE_EXISTING);
                                            } catch (IOException | InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            // log.info("batch file processing done");
                                        });
                            }
                        })
                .start();
    }
}
