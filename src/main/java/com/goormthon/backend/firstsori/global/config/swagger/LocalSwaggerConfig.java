package com.goormthon.backend.firstsori.global.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Map;
import java.util.TreeMap;

@OpenAPIDefinition(
        servers = @Server(url = "http://localhost:8080")
)
@Profile("local")
@Configuration
public class LocalSwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwt = "JWT";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);
        Components components = new Components()
                .addSecuritySchemes(jwt, new SecurityScheme()
                        .name(jwt)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("Bearer")
                        .bearerFormat("JWT"))
                .addSchemas("ApiResponse", new Schema<>()
                        .type("object")
                        .addProperty("success", new Schema<>().type("boolean"))
                        .addProperty("code", new Schema<>().type("integer"))
                        .addProperty("message", new Schema<>().type("string"))
                        .addProperty("data", new Schema<>().type("object")));
        return new OpenAPI()
                .components(components)
                .info(apiInfo())
                .addSecurityItem(securityRequirement);
    }

    private Info apiInfo() {
        return new Info()
                .version("1.0")
                .title("올해의 첫소리 API")
                .description("올해의 첫소리 API 입니다");
    }

    // 전역 응답 스키마 정의
    @Bean
    public OpenApiCustomizer addGlobalResponseSchemas() {
        return openApi -> {
            // 성공 응답용 ApiResponse 스키마
            Schema<?> successApiResponseSchema = new Schema<>()
                    .type("object")
                    .addProperty("success", new Schema<>().type("boolean").example(true).description("성공 여부"))
                    .addProperty("code", new Schema<>().type("integer").example(200).description("HTTP 상태 코드"))
                    .addProperty("message", new Schema<>().type("string").example("호출이 성공적으로 완료되었습니다.").description("응답 메시지"))
                    .addProperty("data", new Schema<>().type("object").nullable(true).description("응답 데이터"));
            
            // ApiResponse<String> 스키마 (인증 API용)
            Schema<?> apiResponseStringSchema = new Schema<>()
                    .type("object")
                    .addProperty("success", new Schema<>().type("boolean").example(true).description("성공 여부"))
                    .addProperty("code", new Schema<>().type("integer").example(200).description("HTTP 상태 코드"))
                    .addProperty("message", new Schema<>().type("string").example("호출이 성공적으로 완료되었습니다.").description("응답 메시지"))
                    .addProperty("data", new Schema<>().type("string").example("재발급 완료").description("응답 데이터"));
            
            // 에러 응답용 ApiResponse 스키마들 (실제 서버 응답 구조에 맞춤)
            Schema<?> error400Schema = new Schema<>()
                    .type("object")
                    .addProperty("success", new Schema<>().type("boolean").example(false).description("성공 여부"))
                    .addProperty("code", new Schema<>().type("integer").example(400001).description("커스텀 에러 코드"))
                    .addProperty("message", new Schema<>().type("string").example("요청 데이터가 유효하지 않습니다.").description("에러 메시지"));
            
            Schema<?> error401Schema = new Schema<>()
                    .type("object")
                    .addProperty("success", new Schema<>().type("boolean").example(false).description("성공 여부"))
                    .addProperty("code", new Schema<>().type("integer").example(401001).description("커스텀 에러 코드"))
                    .addProperty("message", new Schema<>().type("string").example("인증이 필요합니다.").description("에러 메시지"));
            
            Schema<?> error404Schema = new Schema<>()
                    .type("object")
                    .addProperty("success", new Schema<>().type("boolean").example(false).description("성공 여부"))
                    .addProperty("code", new Schema<>().type("integer").example(404004).description("커스텀 에러 코드"))
                    .addProperty("message", new Schema<>().type("string").example("유저의 보드를 찾을 수 없습니다.").description("에러 메시지"));
            
            Schema<?> error500Schema = new Schema<>()
                    .type("object")
                    .addProperty("success", new Schema<>().type("boolean").example(false).description("성공 여부"))
                    .addProperty("code", new Schema<>().type("integer").example(500001).description("커스텀 에러 코드"))
                    .addProperty("message", new Schema<>().type("string").example("서버 내부 오류가 발생했습니다.").description("에러 메시지"));
            
            openApi.getComponents()
                    .addSchemas("ApiResponse", successApiResponseSchema)
                    .addSchemas("ApiResponseString", apiResponseStringSchema)
                    .addSchemas("ApiResponse400", error400Schema)
                    .addSchemas("ApiResponse401", error401Schema)
                    .addSchemas("ApiResponse404", error404Schema)
                    .addSchemas("ApiResponse500", error500Schema);
            
            // 모든 경로에 대해 공통 응답 스키마 적용
            openApi.getPaths().forEach((path, pathItem) -> {
                if (pathItem.getGet() != null) {
                    addCommonResponses(pathItem.getGet().getResponses());
                }
                if (pathItem.getPost() != null) {
                    addCommonResponses(pathItem.getPost().getResponses());
                }
                if (pathItem.getPut() != null) {
                    addCommonResponses(pathItem.getPut().getResponses());
                }
                if (pathItem.getDelete() != null) {
                    addCommonResponses(pathItem.getDelete().getResponses());
                }
            });
        };
    }
    
    private void addCommonResponses(io.swagger.v3.oas.models.responses.ApiResponses responses) {
        if (responses == null) return;
        
        // 각 에러 코드에 맞는 ApiResponse 스키마 적용
        String[][] errorConfigs = {
            {"400", "ApiResponse400"},
            {"401", "ApiResponse401"}, 
            {"404", "ApiResponse404"},
            {"500", "ApiResponse500"}
        };
        
        for (String[] config : errorConfigs) {
            String code = config[0];
            String schemaRef = config[1];
            
            if (responses.containsKey(code)) {
                io.swagger.v3.oas.models.responses.ApiResponse response = responses.get(code);
                if (response.getContent() != null) {
                    response.getContent().forEach((mediaType, content) -> {
                        // 에러 응답에는 해당하는 ApiResponse 스키마 적용
                        content.setSchema(new io.swagger.v3.oas.models.media.Schema<>().$ref("#/components/schemas/" + schemaRef));
                    });
                } else {
                    // Content가 없는 경우 새로 생성
                    io.swagger.v3.oas.models.media.Content newContent = new io.swagger.v3.oas.models.media.Content();
                    newContent.addMediaType("application/json", 
                        new io.swagger.v3.oas.models.media.MediaType()
                            .schema(new io.swagger.v3.oas.models.media.Schema<>().$ref("#/components/schemas/" + schemaRef)));
                    response.setContent(newContent);
                }
            }
        }
    }

    /// 스키마 이름 기준 오름차순
    @Bean
    public OpenApiCustomizer sortSchemasAlphabetically() {
        return openApi -> {
            Map<String, Schema> schemas = openApi.getComponents().getSchemas();
            openApi.getComponents().setSchemas(new TreeMap<>(schemas));
        };
    }
}
