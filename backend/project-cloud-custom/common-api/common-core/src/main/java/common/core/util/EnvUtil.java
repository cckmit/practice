package common.core.util;

import common.core.constant.enums.EnvEnums;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author asd <br>
 * @create 2021-12-03 1:53 PM <br>
 * @project mc-middleware-api <br>
 */
@Slf4j
@Component
@Data
public class EnvUtil {

    private static EnvEnums env;

    @Value("${spring.profiles.active:}")
    public void setEnv(String env) {
        EnvUtil.env = EnvEnums.getByName(env);
    }

    public static EnvEnums getEnv() {
        return env;
    }
}
