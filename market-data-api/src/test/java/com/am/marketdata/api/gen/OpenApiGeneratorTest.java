package com.am.marketdata.api.gen;

import com.am.marketdata.api.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OpenApiGeneratorTest.TestApplication.class, properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "springdoc.api-docs.enabled=true",
        "springdoc.api-docs.path=/v3/api-docs",
        "jwt.secret=test-secret-for-openapi-generation",
        "jwt.expiration=3600000"
})
@AutoConfigureMockMvc
public class OpenApiGeneratorTest {

    @SpringBootApplication
    @ComponentScan(basePackages = "com.am.marketdata.api")
    static class TestApplication {
    }

    @Autowired
    private MockMvc mockMvc;

    // Mock all service beans that controllers depend on
    @MockBean
    private MarketDataFetchService marketDataFetchService;

    @MockBean
    private BrokerageCalculatorApiService brokerageCalculatorApiService;

    @MockBean
    private MarginCalculatorApiService marginCalculatorApiService;

    @MockBean
    private SecurityApiService securityApiService;

    @MockBean
    private MarketIndexApiService marketIndexApiService;

    @MockBean
    private StockIndicesApiService stockIndicesApiService;

    @MockBean
    private MarketAnalyticsApiService marketAnalyticsApiService;

    @MockBean
    private MarketDataPollingApiService marketDataPollingApiService;

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
