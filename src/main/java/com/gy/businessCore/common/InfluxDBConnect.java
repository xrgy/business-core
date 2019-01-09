package com.gy.businessCore.common;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Created by gy on 2019/1/9.
 */
@Component
public class InfluxDBConnect {

    private static final String HTTP="http://";

    InfluxDB influxDB;

    String influxDbName;

    public InfluxDBConnect(){
        String dbUserName = System.getenv("INFLUXDB_USERNAME");
        String dbPassword = System.getenv("INFLUXDB_PASSWORD");

        String dbEndpoint =System.getenv("INFLUXDB_ENDPOINT");
        influxDbName =System.getenv("INFLUXDB_DATABASE_NAME");
        String []str =dbEndpoint.split(":");

//        String ip = ProperUtil.getClusterIpByServiceName(str[0]);
        String ip="47.94.157.199";
        System.out.println(ip);
        String openurl = HTTP+ip+":"+str[1];
        if(influxDB == null){
            influxDB = InfluxDBFactory.connect(openurl, dbUserName, dbPassword);
        }
    }

    /**
     * 插入数据到inflxudb
     * @param measurement
     * @param tags
     * @param fields
     */
    public void insertInfluxdb(String measurement, Map<String, String> tags, Map<String, Object> fields) {
        Point point = Point.measurement(measurement)
                .tag(tags)
                .fields(fields)
                .build();
        influxDB.write(influxDbName, "", point);
    }

    /**
     * 执行查询语句
     * @param command
     * @return
     */
    public QueryResult query(String command){
        return influxDB.query(new Query(command, influxDbName));
    }

}
