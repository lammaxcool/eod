package org.kpi.dedup.redis;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class RedisContainer extends GenericContainer<RedisContainer> {

    private static final int DEFAULT_REDIS_PORT = 6379;
    private static final DockerImageName DEFAULT_REDIS_IMAGE = DockerImageName.parse("redis:7.2.3-alpine");

    public RedisContainer() {
        super(DEFAULT_REDIS_IMAGE);
        withExposedPorts(DEFAULT_REDIS_PORT);
    }
}