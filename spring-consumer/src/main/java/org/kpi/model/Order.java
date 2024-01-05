package org.kpi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Order(
        @JsonProperty("ORDER_ID") long orderId,
        @JsonProperty("CODE") String code,
        @JsonProperty("STORE_ID") String storeId,
        @JsonProperty("USER_ID") String userId,
        @JsonProperty("QUANTITY") int quantity
) {
}