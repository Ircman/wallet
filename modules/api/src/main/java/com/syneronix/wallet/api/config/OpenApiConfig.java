package com.syneronix.wallet.api.config;



import com.syneronix.wallet.api.errors.BadRequestErrorModel;
import com.syneronix.wallet.api.errors.ErrorResponse;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


@OpenAPIDefinition(
        info = @Info(
                title = "Syneronix wallet",
                version = "1",
                contact = @Contact(name = "Syneronix team", url = "https://github.com/Ircman", email = "syneronix@gmail.com"),
                description = "Syneronix wallet API Documentation",
                license = @License(name = "Syneronix", url = "https://syneronix.github.io/")
        ),
        servers = {
                @io.swagger.v3.oas.annotations.servers.Server(url = "http://127.0.0.1:8080", description = "LocalHost")
        }
)
@Configuration
public class OpenApiConfig {



    @Bean
    public OpenAPI customizeOpenAPI() {

        final String validationSchemaName = BadRequestErrorModel.ValidationError.class.getSimpleName();

        Schema validationErrorSchema = new ObjectSchema()
                .name(validationSchemaName) // Имя схемы
                .description("Details of a single field validation error")
                .addProperty("field", new Schema<String>().type("string").example("username"))
                .addProperty("message", new Schema<String>().type("string").example("must not be empty"));

        return new OpenAPI()
                .servers(List.of(new Server().url("/")))
                .components(new Components()
                        .addSchemas(ErrorResponse.class.getSimpleName(), new ObjectSchema()
                                .addProperty("code", new Schema<String>().type("string").example("NOT_FOUND"))
                                .addProperty("errorCode", new Schema<Integer>().type("integer").example(404))
                                .addProperty("message", new Schema<String>().type("string").example("Detailed error message"))
                                .addProperty("path", new Schema<String>().type("string").example("/api/v1/path"))
                        )

                        .addSchemas(validationSchemaName, validationErrorSchema)
                        .addSchemas(BadRequestErrorModel.class.getSimpleName(), new ObjectSchema()
                                .addProperty("code", new Schema<String>().type("string").example("BAD_REQUEST"))
                                .addProperty("description", new Schema<String>().type("string").example("One or more fields have invalid values"))
                                .addProperty("validationError", new ArraySchema()
                                        .description("List of field validation errors")
                                        .items(new Schema<BadRequestErrorModel.ValidationError>().$ref("#/components/schemas/" + validationSchemaName))
                                        .example(List.of(
                                                Map.of("field", "email", "message", "must be a valid email address")
                                        ))
                                )
                        )
                );
    }

    private static final Comparator<Entry<PathItem.HttpMethod, Operation>> HTTP_METHOD_COMPARATOR = (entry1, entry2) -> {
        List<PathItem.HttpMethod> order = List.of(
                PathItem.HttpMethod.GET,
                PathItem.HttpMethod.POST,
                PathItem.HttpMethod.PUT,
                PathItem.HttpMethod.DELETE,
                PathItem.HttpMethod.PATCH,
                PathItem.HttpMethod.OPTIONS,
                PathItem.HttpMethod.HEAD,
                PathItem.HttpMethod.TRACE
        );
        int index1 = order.indexOf(entry1.getKey());
        int index2 = order.indexOf(entry2.getKey());
        return Integer.compare(index1, index2);
    };


    @Bean
    public OpenApiCustomizer globalOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getPaths() != null) {
                List<Map.Entry<String, PathItem>> sortedPaths = new ArrayList<>(openApi.getPaths().entrySet());
                sortedPaths.sort(Map.Entry.comparingByKey());

                Paths customPaths = new Paths();
                for (Map.Entry<String, PathItem> pathEntry : sortedPaths) {
                    PathItem pathItem = pathEntry.getValue();
                    Map<PathItem.HttpMethod, Operation> operations = pathItem.readOperationsMap();

                    List<Entry<PathItem.HttpMethod, Operation>> sortedOperations = new ArrayList<>(operations.entrySet());
                    sortedOperations.sort(HTTP_METHOD_COMPARATOR);

                    PathItem sortedPathItem = new PathItem();
                    sortedPathItem.set$ref(pathItem.get$ref());
                    sortedPathItem.setExtensions(pathItem.getExtensions());
                    sortedPathItem.setParameters(pathItem.getParameters());

                    for (Entry<PathItem.HttpMethod, Operation> opEntry : sortedOperations) {
                        sortedPathItem.operation(opEntry.getKey(), opEntry.getValue());
                    }

                    customPaths.addPathItem(pathEntry.getKey(), sortedPathItem);
                }
                openApi.setPaths(customPaths);
            }
        };
    }

    @Bean
    public GroupedOpenApi groupedAllApi(

            OpenApiCustomizer globalOpenApiCustomizer
    ) {
        return GroupedOpenApi.builder()
                .group("all")
                .packagesToScan("com.syneronix.wallet.api")

                .addOpenApiCustomizer(globalOpenApiCustomizer) // Применяем сортировку
                .build();
    }

    @Bean
    public GroupedOpenApi publicWebApi(

            OpenApiCustomizer globalOpenApiCustomizer
    ) {
        return GroupedOpenApi.builder()
                .group("private-web")
                .pathsToMatch(
                        "/api/v1/me"
                )

                .addOpenApiCustomizer(globalOpenApiCustomizer)
                .build();
    }


}
