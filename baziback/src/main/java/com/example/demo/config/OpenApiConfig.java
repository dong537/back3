package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Springdoc OpenAPI配置
 * 访问地址：http://localhost:8088/swagger-ui/index.html
 */
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("参天AI - 八字命理分析系统 API")
                        .version("1.0.0")
                        .description("基于AI的传统命理分析平台，提供八字、紫微、易经、塔罗、星座等多种分析服务")
                        .contact(new Contact()
                                .name("参天AI团队")
                                .email("support@cantian.ai")
                                .url("https://cantian.ai"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
