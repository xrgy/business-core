package com.gy.businessCore.service.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gy.businessCore.entity.monitor.OperationMonitorEntity;
import com.gy.businessCore.service.AlertService;
import com.gy.businessCore.service.MonitorService;
import com.gy.businessCore.utils.EtcdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * Created by gy on 2018/3/31.
 */
@Service
public class AlertServiceImpl implements AlertService {

    private String PORT = "8095";
    private String PREFIX = "alerts";
    private static final String HTTP="http://";
    private static final String PATH_GET_SEVERITY_COUNT="getAlertSeverityCountMap";

    @Bean
    public RestTemplate rest(){
        return new RestTemplate();
    }

    @Autowired
    ObjectMapper objectMapper;

    private String alertPrefix(){
        String ip = "";
//        try {
//            return HTTP+"47.105.64.176:30095/"+PREFIX+"/";

            ip="127.0.0.1";
//            ip = EtcdUtil.getClusterIpByServiceName("alert-coll-service");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return HTTP+ip+":"+PORT+"/"+PREFIX+"/";
    }


    @Override
    public Map<String, Integer> getAlertInfoByMonitorUuid(String monitorUuid) {
        ResponseEntity<String> response = rest().getForEntity(alertPrefix()+PATH_GET_SEVERITY_COUNT+"?monitorUuid={1}",String.class,monitorUuid);
        try {
            return objectMapper.readValue(response.getBody(),Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
