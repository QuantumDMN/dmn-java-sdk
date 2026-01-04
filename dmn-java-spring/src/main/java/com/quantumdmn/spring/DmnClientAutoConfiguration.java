package com.quantumdmn.spring;

import com.quantumdmn.client.DmnService;
import com.quantumdmn.client.auth.ZitadelTokenProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * Spring Boot auto-configuration for QuantumDMN client.
 * 
 * <p>Automatically configures a {@link DmnService} bean when:</p>
 * <ul>
 *   <li>{@code quantumdmn.base-url} is set</li>
 *   <li>Either {@code quantumdmn.token} is set or a {@code Supplier<String>} bean named "dmnTokenProvider" exists</li>
 * </ul>
 */
@AutoConfiguration
@EnableConfigurationProperties(DmnClientProperties.class)
@ConditionalOnProperty(prefix = "quantumdmn", name = "base-url")
public class DmnClientAutoConfiguration {

    @Bean(name = "dmnTokenProvider")
    @ConditionalOnMissingBean(name = "dmnTokenProvider")
    @ConditionalOnProperty(prefix = "quantumdmn.auth.zitadel", name = "key-file")
    public Supplier<String> zitadelTokenProvider(DmnClientProperties properties) throws IOException {
        DmnClientProperties.Auth.Zitadel zitadel = properties.getAuth().getZitadel();
        if (zitadel.getKeyFile() == null || zitadel.getKeyFile().isBlank()) {
            return null; 
        }
        return new ZitadelTokenProvider(
            zitadel.getKeyFile(),
            zitadel.getIssuer(),
            zitadel.getProjectId()
        );
    }

    /**
     * Creates a DmnService using a custom token provider bean if available.
     */
    @Bean
    @ConditionalOnMissingBean
    public DmnService dmnService(DmnClientProperties properties, 
                                  @org.springframework.beans.factory.annotation.Autowired(required = false) 
                                  @org.springframework.beans.factory.annotation.Qualifier("dmnTokenProvider") 
                                  Supplier<String> tokenProvider) {
        if (tokenProvider != null) {
            return new DmnService(properties.getBaseUrl(), tokenProvider);
        } else if (properties.getToken() != null && !properties.getToken().isBlank()) {
            return new DmnService(properties.getBaseUrl(), properties.getToken());
        } else {
            throw new IllegalStateException(
                "QuantumDMN configuration requires either 'quantumdmn.token' property or a 'dmnTokenProvider' bean"
            );
        }
    }
}
