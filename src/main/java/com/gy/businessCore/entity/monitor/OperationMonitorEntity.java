package com.gy.businessCore.entity.monitor;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by gy on 2018/5/5.
 */
@Getter
@Setter
public class OperationMonitorEntity {


    private String uuid;

    private String name;

    private String ip;

//    private String highTypeId;

//    private String middleTypeId;

    private String lightType;

    private String monitorType;

    private String monitorInfo;

    private String templateId;

    private String scrapeInterval;

    private String scrapeTimeout;

    private Date createTime;

    private Date updateTime;

    private String extra;

    private int deleted;

}
