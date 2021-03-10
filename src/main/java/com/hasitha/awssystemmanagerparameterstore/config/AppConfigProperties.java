package com.hasitha.awssystemmanagerparameterstore.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.Optional;

@Validated
@Configuration
@ConfigurationProperties
public class AppConfigProperties {

    @Autowired
    private Environment environment;

    @NotBlank
    @Value("${aws-system-manager-parameter-store.secret-manager.region}")
    private String secretManagerRegion;

    public String getSecretManagerRegion() {
        return secretManagerRegion;
    }

    @PostConstruct
    public Optional<String> getActiveProfile() {
        return Arrays.stream(environment.getActiveProfiles()).findAny();
    }
}
