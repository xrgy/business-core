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
        tags.put("source_id",data.getSourceId());
        connect.insertInfluxdb(TABLE_NAME,tags,MapObjectUtil.object2Map(data));
    }

    @Override
    public List<InfluxData> getScoreDataBymonitorAndInterval(String monitorUuid, int interval) {

        String sql = "select * from "+ TABLE_NAME+" where source_id="+"'"+monitorUuid+"'"+" and time>now-"+interval+"d";
        InfluxDBConnect connect = new InfluxDBConnect();
        QueryResult results = connect.query(sql);

        if(results.getResults() == null){
            return null;
        }
//        List<InfluxData> lists = new ArrayList<CodeInfo>();
        for (QueryResult.Result result : results.getResults()) {

            List<QueryResult.Series> series= result.getSeries();
            for (QueryResult.Series serie : series) {
                List<List<Object>>  values = serie.getValues();
                List<String> columns = serie.getColumns();

//                lists.addAll(getQueryData(columns, values));
            }
        }

        return null;
    }
}
