package com.gy.businessCore.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.gy.businessCore.common.InfluxDBConnect;
import com.gy.businessCore.entity.InfluxData;
import com.gy.businessCore.service.AlertService;
import com.gy.businessCore.service.InfluxService;
import com.gy.businessCore.utils.MapObjectUtil;
import com.gy.businessCore.utils.ProperUtil;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by gy on 2018/3/31.
 */
@Service
public class InfluxServiceImpl implements InfluxService {

    private String PORT = "30092";
//    private String PORT = "8086";
    private String PREFIX = "alerts";
    private static final String HTTP="http://";
    private static final String PATH_GET_SEVERITY_COUNT="getAlertSeverityCountMap";
    private static final String TABLE_NAME="tbl_monitor_data";








    @Override
    public void insertResourceScore(InfluxData data) {
        InfluxDBConnect connect = new InfluxDBConnect();
        Map<String,String> tags = new HashMap<>();
        tags.put("source_id",data.getSource_id());
        Map<String,Object> fileds = new HashMap<>();
        fileds.put("busy_score",data.getBusy_score());
        fileds.put("health_score",data.getHealth_score());
        fileds.put("available_score",data.getAvailable_score());
        connect.insertInfluxdb(TABLE_NAME,tags,fileds);
    }

    @Override
    public List<InfluxData> getScoreDataBymonitorAndInterval(String monitorUuid, int interval) {

        String sql = "select * from "+ TABLE_NAME+" where source_id="+"'"+monitorUuid+"'"+" and time>now()-"+interval+"d";
        InfluxDBConnect connect = new InfluxDBConnect();
        QueryResult results = connect.query(sql);

        if(results.getResults() == null){
            return null;
        }
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        List<InfluxData> list = resultMapper.toPOJO(results, InfluxData.class);
        return list;
    }
}
