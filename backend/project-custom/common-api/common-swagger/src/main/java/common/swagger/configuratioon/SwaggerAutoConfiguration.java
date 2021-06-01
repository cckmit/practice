package common.swagger.configuratioon;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.ulisesbocchio.jasyptspringboot.annotation.ConditionalOnMissingBean;
import common.swagger.configuratioon.properties.SwaggerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zack <br>
 * @create 2021-06-01 16:32 <br>
 * @project common-swagger <br>
 */
@Slf4j
@Configuration
@EnableSwagger2
@EnableAutoConfiguration
@Profile({"dev"})
@ConditionalOnProperty(name = "swagger.enabled", matchIfMissing = true)
public class SwaggerAutoConfiguration {
    /** 默认的排除路径，排除Spring Boot默认的错误处理路径和端点 */
    private static final List<String> DEFAULT_EXCLUDE_PATH =
            Arrays.asList("/error", "/actuator/**");

    private static final String BASE_PATH = "/**";
    private static final String LOCALHOST = "localhost";

    @Bean
    @ConditionalOnMissingBean
    public SwaggerProperties swaggerProperties() {
        return new SwaggerProperties();
    }

    @Bean
    public Docket api(SwaggerProperties swaggerProperties) {
        if (swaggerProperties.getBasePath().isEmpty()) {
            swaggerProperties.getBasePath().add(BASE_PATH);
        }
        swaggerProperties.getExcludePath().removeAll(DEFAULT_EXCLUDE_PATH);
        swaggerProperties.getExcludePath().addAll(DEFAULT_EXCLUDE_PATH);

        List<Predicate<String>> basePath =
                swaggerProperties.getBasePath().stream()
                        .map(PathSelectors::ant)
                        .collect(Collectors.toList());

        List<Predicate<String>> excludePath =
                swaggerProperties.getExcludePath().stream()
                        .map(PathSelectors::ant)
                        .collect(Collectors.toList());

        return new Docket(DocumentationType.SWAGGER_2)
                .host(swaggerProperties.getHost())
                .apiInfo(apiInfo(swaggerProperties))
                .select()
                .apis(RequestHandlerSelectors.basePackage(swaggerProperties.getBasePackage()))
                // .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(
                        Predicates.and(
                                Predicates.not(Predicates.or(excludePath)),
                                Predicates.or(basePath)))
                .build()
                .securitySchemes(Collections.singletonList(securitySchema()))
                .securityContexts(Collections.singletonList(securityContext()))
                .pathMapping(swaggerProperties.getPathMapping());
    }

    /**
     * 配置默认的全局鉴权策略的开关，通过正则表达式进行匹配；默认匹配所有URL
     *
     * @return
     */
    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(
                        PathSelectors.regex(swaggerProperties().getAuthorization().getAuthRegex()))
                .build();
    }

    /**
     * 默认的全局鉴权策略
     *
     * @return
     */
    private List<SecurityReference> defaultAuth() {
        List<AuthorizationScope> authorizationScopeList =
                swaggerProperties().getAuthorization().getAuthorizationScopeList().stream()
                        .map(x -> new AuthorizationScope(x.getScope(), x.getDescription()))
                        .collect(Collectors.toList());

        return Collections.singletonList(
                SecurityReference.builder()
                        .reference(swaggerProperties().getAuthorization().getName())
                        .scopes(authorizationScopeList.toArray(new AuthorizationScope[0]))
                        .build());
    }

    private OAuth securitySchema() {
        List<AuthorizationScope> authorizationScopeList =
                swaggerProperties().getAuthorization().getAuthorizationScopeList().stream()
                        .map(x -> new AuthorizationScope(x.getScope(), x.getDescription()))
                        .collect(Collectors.toList());
        List<GrantType> grantTypes =
                swaggerProperties().getAuthorization().getTokenUrlList().stream()
                        .map(ResourceOwnerPasswordCredentialsGrant::new)
                        .collect(Collectors.toList());

        return new OAuth(
                swaggerProperties().getAuthorization().getName(),
                authorizationScopeList,
                grantTypes);
    }

    private ApiInfo apiInfo(SwaggerProperties swaggerProperties) {
        return new ApiInfoBuilder()
                .title(swaggerProperties.getTitle())
                .description(swaggerProperties.getDescription())
                .version(swaggerProperties.getVersion())
                .build();
    }
}
