package com.hasitha.awssystemmanagerparameterstore.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.stream.Collectors;

@Configuration
public class WebConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebConfig.class);

    private static final String DATASOURCE_URL = "url";
    private static final String DATASOURCE_USERNAME = "username";
    private static final String DATASOURCE_PASSWORD = "password";

    private final AppConfigProperties properties;

    WebConfig(AppConfigProperties properties) {
        this.properties = properties;

    }

    @Bean
    @Profile({ "dev", "prod" })
    public DataSource dataSource(AWSSimpleSystemsManagement awsSsmClient) {

        Properties datasourceProperties = loadDatasourceParameters(awsSsmClient);

        return DataSourceBuilder.create().url(datasourceProperties.getProperty(DATASOURCE_URL))
                .username(datasourceProperties.getProperty(DATASOURCE_USERNAME)).password(datasourceProperties.getProperty(DATASOURCE_PASSWORD))
                .build();
    }

    @Bean
    public AWSSimpleSystemsManagement awsSsmClient() {
        return
                AWSSimpleSystemsManagementClientBuilder.standard().withRegion(properties.getSecretManagerRegion())
                        .withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTPS)).build();
    }

    private Properties loadDatasourceParameters(AWSSimpleSystemsManagement awsSsmClient) {

        String activeProfile = properties.getActiveProfile().orElse("default");
        String datasourceParamPath = String.format("/api/%s/data-source/", activeProfile);
        var datasourceProperties = new Properties();

        GetParametersByPathResult getParameterResult = awsSsmClient.getParametersByPath(new GetParametersByPathRequest().withPath(datasourceParamPath).withRecursive(true).withWithDecryption(true));

        datasourceProperties.putAll(getParameterResult.getParameters().stream().collect(
                Collectors.toMap(param -> param.getName().replaceFirst(datasourceParamPath, ""),
                        Parameter::getValue)));
        return datasourceProperties;

    }
}
