package com.syneronix.wallet.spring;

import java.math.BigDecimal;
import java.util.TimeZone;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vladmihalcea.hibernate.type.util.ObjectMapperSupplier;

import lombok.val;

@Component
public class CustomObjectMapperSupplier implements ObjectMapperSupplier {
    private static final String UTC_TIME_ZONE = "UTC";

    @Override
    public ObjectMapper get() {
        val mapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                .configure(MapperFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL, true)
                .setTimeZone(TimeZone.getTimeZone(UTC_TIME_ZONE))
                .registerModule(new JavaTimeModule());
        mapper.configOverride(BigDecimal.class).setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.STRING));
        return mapper;
    }
}
