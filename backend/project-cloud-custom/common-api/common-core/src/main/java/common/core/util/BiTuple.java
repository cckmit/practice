package common.core.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author asd <br>
 * @create 2021-12-03 3:53 PM <br>
 * @project project-cloud-custom <br>
 */
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BiTuple<F, S> {
    private F f;
    private S s;
}
