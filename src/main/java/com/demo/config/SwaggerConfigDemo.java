package com.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2                // Swagger的开关，表示已经启用Swagger
@Configuration
public class SwaggerConfigDemo {

//        @Value("${swagger.basePackage}")
//        private String basePackage;       // controller接口所在的包
//
//        @Value("${swagger.title}")
//        private String title;           // 当前文档的标题
//
//        @Value("${swagger.description}")
//        private String description;         // 当前文档的详细描述
//
//        @Value("${swagger.version}")
//        private String version;         // 当前文档的版本

        @Bean
        public Docket createRestApi() {
            return new Docket(DocumentationType.SWAGGER_2)
                    .apiInfo(apiInfo())
                    .select()
                    .apis(RequestHandlerSelectors.basePackage("com.example.demo"))
                    .paths(PathSelectors.any())
                    .build();
        }

        private ApiInfo apiInfo() {
            return new ApiInfoBuilder()
                    .title("springBoot-demo")
                    .description("springBoot-demo desc")
                    .version("1.0")
                    .build();
        }
}
