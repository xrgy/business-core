package com.gy.businessCore.service;

import com.gy.businessCore.entity.InfluxData;

import java.util.List;
import java.util.Map;


/**
 * Created by gy on 2018/3/31.
 */
public interface InfluxService {

    void insertResourceScore(InfluxData data);


    List<InfluxData> getScoreDataBymonitorAndInterval(String monitorUuid,int interval);



}
