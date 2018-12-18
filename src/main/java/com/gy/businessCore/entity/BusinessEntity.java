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
@Table(name = "tbl_business")
public class BusinessEntity {

    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "name")
    private String name;

    @Column(name = "busy_score")
    private float busy_score;

    @Column(name = "health_score")
    private float health_score;

    @Column(name = "available_score")
    private float available_score;

    @Column(name = "cluster")
    private String cluster;


}
