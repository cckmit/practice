package custom.basic.controller.admin;

import common.core.annotation.Inner;
import common.core.util.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zack <br>
 * @create 2021-06-27<br>
 * @project project-cloud-custom <br>
 */
@RestController
@RequestMapping("/basic/admin/ping")
public class PingAdminController {

    @Inner
    @GetMapping
    public R<String> ping() {
        return R.success("pong");
    }
}
