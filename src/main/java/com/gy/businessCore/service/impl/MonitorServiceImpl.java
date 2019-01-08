package com.gy.businessCore.service.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gy.businessCore.common.BusinessEnum;
import com.gy.businessCore.dao.BusinessDao;
import com.gy.businessCore.entity.*;
import com.gy.businessCore.entity.monitor.OperationMonitorEntity;
import com.gy.businessCore.service.BusinessService;
import com.gy.businessCore.service.MonitorService;
import com.gy.businessCore.service.WeavescopeService;
import com.gy.businessCore.utils.EtcdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;


/**
 * Created by gy on 2018/3/31.
 */
@Service
public class MonitorServiceImpl implements MonitorService {

    private String PORT = "8084";
    private String PREFIX = "monitor";
    private String Light_PATH = "getLightType";
    private static final String HTTP="http://";
    private static final String PATH_GET_ALL_MONITOR_RECORD="getAllMonitorRecord";
    private static final String PATH_GET_QUOTA_VALUE="getQuotaValueByName";

    @Bean
    public RestTemplate rest(){
        return new RestTemplate();
    }

    @Autowired
    ObjectMapper objectMapper;

    private String monitorPrefix(){
        String ip = "";
        try {
            ip="127.0.0.1";
//            ip = EtcdUtil.getClusterIpByServiceName("monitor-core-service");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return HTTP+ip+":"+PORT+"/"+PREFIX+"/";
    }

    @Override
    public List<OperationMonitorEntity> getAllMonitorRecord() {
        ResponseEntity<String> response = rest().getForEntity(monitorPrefix()+PATH_GET_ALL_MONITOR_RECORD,String.class);
        try {
            return objectMapper.readValue(response.getBody(),new TypeReference<List<OperationMonitorEntity>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getQuotaValue(String monitorUuid,String quotaName) {
        String quotaValue = rest().getForObject(monitorPrefix()+PATH_GET_QUOTA_VALUE+"?monitorUUid={1}&quotaName={2}",String.class,monitorUuid,quotaName);
        return quotaValue;
    }
}
