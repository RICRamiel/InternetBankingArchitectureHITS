package org.ricramiel.coreapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Retryer;
import feign.Util;
import feign.codec.Decoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Configuration
public class FeignConfig {
    @Bean
    public Decoder feingDecoder() {
        return ((response, type) -> {
            String jasc = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            String json = jasc.replaceAll("^[^{]*", "").replaceAll("[^}]*$", "");
            if (type instanceof Class) {
                return new ObjectMapper().readValue(json, (Class<?>) type);
            }
            return new ObjectMapper().readValue(json, new ObjectMapper().constructType(type));
        });
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(1000, 2000, 3);
    }
}