package com.gy.businessCore.service;

import com.gy.businessCore.entity.monitor.OperationMonitorEntity;

import java.util.List;
import java.util.Map;


/**
 * Created by gy on 2018/3/31.
 */
public interface AlertService {

    Map<String,Integer> getAlertInfoByMonitorUuid(String monitorUuid);
}
