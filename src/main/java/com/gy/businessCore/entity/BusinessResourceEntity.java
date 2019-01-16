package com.gy.businessCore.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * Created by gy on 2018/3/31.
 */
@Data
@Entity
@Table(name = "tbl_business_resource")
public class BusinessResourceEntity {

    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "business_uuid")
    private String businessUuid;

    @Column(name = "monitor_id")
    private String monitorId;

    @Column(name = "busy_score")
    private double busy_score;

    @Column(name = "health_score")
    private double health_score;

    @Column(name = "available_score")
    private double available_score;

    @Column(name = "light_type")
    private String lightType;



}
