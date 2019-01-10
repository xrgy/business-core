package com.gy.businessCore.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.Setter;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import retrofit2.http.GET;

import java.time.Instant;

/**
 * Created by gy on 2019/1/9.
 */
@Getter
@Setter
@Measurement(name = "tbl_monitor_data")
public class InfluxData {

    @Column(name = "time")
    private Instant time;

    @Column(name = "source_id",tag = true)
    private String source_id;

    @Column(name = "busy_score")
    private double busy_score;

    @Column(name = "health_score")
    private double health_score;

    @Column(name = "available_score")
    private double available_score;
}
