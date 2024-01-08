package org.kpi.kafka;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("application.kafka")
public record KafkaApplicationProperties(
        @NotBlank String topic,
        @NotBlank String bootstrapServers,
        @NotBlank String isolationLevel,
        @NotBlank String enableAutoCommit,
        @Positive int partitionCount,
        @Positive int replicaCount
) {
}