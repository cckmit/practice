package custom.gateway.configuration.routes;

import custom.gateway.configuration.swagger.SwaggerResourceHandler;
import custom.gateway.configuration.swagger.SwaggerSecurityHandler;
import custom.gateway.configuration.swagger.SwaggerUiHandler;
import custom.gateway.handler.CaptchaHandler;
import custom.gateway.handler.HystrixFallbackHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

import javax.annotation.Resource;
import java.net.URI;

/**
 * @author zack <br>
 * @create 2021-06-26<br>
 * @project project-cloud-custom <br>
 */
@Slf4j
@Configuration
public class RouterConfiguration {
    private static HandlerFunction<ServerResponse> redirectMapping =
            serverRequest ->
                    ServerResponse.temporaryRedirect(URI.create("/swagger-ui.html")).build();
    @Resource private HystrixFallbackHandler hystrixFallbackHandler;
    @Resource private CaptchaHandler captchaHandler;
    @Resource private SwaggerResourceHandler swaggerResourceHandler;
    @Resource private SwaggerSecurityHandler swaggerSecurityHandler;
    @Resource private SwaggerUiHandler swaggerUiHandler;

    @Bean
    public RouterFunction captchaFunction() {
        return RouterFunctions.route(RequestPredicates.GET("/code"), captchaHandler);
    }

    @Bean
    public RouterFunction hystrixFallbackFunction() {
        return RouterFunctions.route(RequestPredicates.path("/fallback"), hystrixFallbackHandler);
    }

    @Bean
    public RouterFunction swaggerFunction() {
        RouterFunction routes =
                RouterFunctions.route(RequestPredicates.GET("/configuration/ui"), swaggerUiHandler)
                        .andRoute(
                                RequestPredicates.GET("/configuration/security"),
                                swaggerSecurityHandler);

        routes = routes.andRoute(RequestPredicates.GET(""), swaggerResourceHandler);

        return RouterFunctions.nest(RequestPredicates.path("/swagger-resources"), routes);
    }

    @Bean
    public RouterFunction<ServerResponse> apiRoute() {
        return RouterFunctions.route()
                .GET("/swagger-ui", redirectMapping)
                .GET("/api", redirectMapping)
                .build();
    }
}
