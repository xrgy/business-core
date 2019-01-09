package com.gy.businessCore.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.Setter;
import retrofit2.http.GET;

/**
 * Created by gy on 2019/1/9.
 */
@Getter
@Setter
public class InfluxData {

    @JsonProperty("source_id")
    private String sourceId;

    @JsonProperty("busy_score")
    private String busyScore;

    @JsonProperty("health_score")
    private String healthScore;

    @JsonProperty("available_score")
    private String availableScore;
}
