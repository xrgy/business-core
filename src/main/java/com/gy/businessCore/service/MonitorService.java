package com.gy.businessCore.service;

import com.gy.businessCore.entity.BusinessEntity;
import com.gy.businessCore.entity.BusinessResourceEntity;
import com.gy.businessCore.entity.TestEntity;
import com.gy.businessCore.entity.WeaveContainerImageInfo;
import com.gy.businessCore.entity.monitor.OperationMonitorEntity;

import java.util.List;


/**
 * Created by gy on 2018/3/31.
 */
public interface MonitorService {
    List<OperationMonitorEntity> getAllMonitorRecord();

    String getQuotaValue(String monitorUuid,String quotaName);
}
