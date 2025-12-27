package com.am.marketdata.api.gen;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OpenApiGeneratorTest {

    @SpringBootApplication
    @org.springframework.context.annotation.ComponentScan(basePackages = "com.am.marketdata.api")
    static class TestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void generateOpenApiSpec() throws Exception {
        byte[] specBytes = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        Path targetDir = Paths.get("target");
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        Path path = targetDir.resolve("openapi.json");
        Files.write(path, specBytes);
        System.out.println("Generated OpenAPI Spec at: " + path.toAbsolutePath());
    }
}
