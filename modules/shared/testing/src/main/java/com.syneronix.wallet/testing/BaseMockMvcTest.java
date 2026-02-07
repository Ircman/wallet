package com.syneronix.wallet.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest(
        classes = IntegrationApplication.class,
        properties = "classpath:application-itest.yml",
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@ActiveProfiles(resolver = ItestActiveProfilesResolver.class)
public class BaseMockMvcTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    protected static UUID uuid() {
        return UUID.randomUUID();
    }

    protected static Instant instantNow() {
        return Instant.now();
    }

    @SneakyThrows
    protected String toJson(Object obj) {
        return objectMapper.writeValueAsString(obj);
    }

}
