package top.hubby.lock.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hubby.lock.model.Data;
import top.hubby.lock.model.Interesting;

import java.util.stream.IntStream;

/**
 * @author asd <br>
 * @create 2021-10-20 10:16 AM <br>
 * @project swagger-3 <br>
 */
@Api(tags = "Lock")
@Slf4j
@RestController
@RequestMapping("/lock/scope")
public class LockScopeController {
    @GetMapping("/wrong")
    public int wrong(@RequestParam(value = "count", defaultValue = "1000000") int count) {
        Data.reset();
        IntStream.rangeClosed(1, count).parallel().forEach(i -> new Data().wrong());
        return Data.getCounter();
    }

    @GetMapping("/right")
    public int right(@RequestParam(value = "count", defaultValue = "1000000") int count) {
        Data.reset();
        IntStream.rangeClosed(1, count).parallel().forEach(i -> new Data().right());
        return Data.getCounter();
    }

    @GetMapping("/wrong2")
    public String wrong2() {
        Interesting interesting = new Interesting();
        new Thread(() -> interesting.add()).start();
        new Thread(() -> interesting.compare()).start();
        return "OK";
    }

    @GetMapping("/right2")
    public String right2() {
        Interesting interesting = new Interesting();
        new Thread(() -> interesting.add()).start();
        new Thread(() -> interesting.compareRight()).start();
        return "OK";
    }
}
