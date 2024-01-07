package org.kpi.dedup.redis;

import io.lettuce.core.resource.ClientResources;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
@Profile(value = {"redis", "test"})
@EnableConfigurationProperties(RedisProperties.class)
class RedisConfiguration {

    @Bean
    public ClientResources clientResources() {
        return ClientResources.create();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(RedisProperties redisProperties, ClientResources clientResources) {
        var clientConfig = lettuceClientConfig(redisProperties, clientResources);
        var standaloneConfiguration = new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());
        standaloneConfiguration.setPassword(RedisPassword.of(redisProperties.getPassword()));

        return new LettuceConnectionFactory(standaloneConfiguration, clientConfig);
    }

    private static LettuceClientConfiguration lettuceClientConfig(RedisProperties redisProperties, ClientResources clientResources) {
        return LettuceClientConfiguration.builder()
                .commandTimeout(redisProperties.getTimeout())
                .clientResources(clientResources)
                .build();
    }
}