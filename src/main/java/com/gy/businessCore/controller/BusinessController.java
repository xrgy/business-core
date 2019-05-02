package com.gy.businessCore.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gy.businessCore.entity.*;
import com.gy.businessCore.service.BusinessService;
import com.gy.businessCore.service.InfluxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * Created by gy on 2018/3/31.
 */
@RestController
@RequestMapping("business")
public class BusinessController {

    @Autowired
    private BusinessService service;

    @Autowired
    private InfluxService influxService;

    @Autowired
    private ObjectMapper mapper;

    @RequestMapping("jpa")
    @ResponseBody
    public TestEntity testJPA(HttpServletRequest request){
//        TestEntity entity = new TestEntity();
//        entity.setId("sasada");
//        entity.setName("gygy");
//        return entity;
        return service.getJPAInfo();
    }

    @RequestMapping("getWeaveInfo")
    @ResponseBody
    public String getWeaveContainerImage() throws JsonProcessingException {
//        return service.getWeaveContainerImage();
        return mapper.writeValueAsString(service.getWeaveContainerImage());
    }

    @RequestMapping("getBusinessList")
    @ResponseBody
    public String getBusinessList() throws JsonProcessingException {
        return mapper.writeValueAsString(service.getBusinessList());
    }

    @RequestMapping("addBusinessResource")
    @ResponseBody
    public boolean insertBusinessResource(@RequestBody String data) throws IOException {
        BusinessResourceEntity resourceEntity =mapper.readValue(data,BusinessResourceEntity.class);
        return service.insertBusinessResource(resourceEntity);
    }

    @RequestMapping("addBusinessResourceList")
    @ResponseBody
    public boolean insertBusinessResourceList(@RequestBody String data) throws IOException {
        List<BusinessResourceEntity> resourceEntityList = mapper.readValue(data,
                new TypeReference<List<BusinessResourceEntity>>(){});
        return service.insertBusinessResourceList(resourceEntityList);
    }
    @RequestMapping("addBusiness")
    @ResponseBody
    public boolean addBusiness(@RequestBody String data) throws IOException {
        BusinessEntity business = mapper.readValue(data, BusinessEntity.class);
        service.insertBusiness(business);
        return true;
    }
    @RequestMapping("getBusinessResourcesByBusinessId")
    @ResponseBody
    public String getBusinessResourcesByBusinessId(String businessId) throws JsonProcessingException {
        return mapper.writeValueAsString(service.getBusinessResourcesByBusinessId(businessId));
    }

    @RequestMapping("insertResourceScore")
    @ResponseBody
    public void insertResourceScore(){
//        InfluxData data = new InfluxData();
//        data.setSourceId("11111");
//        data.setBusyScore("48.78");
//        data.setAvailableScore("57.97");
//        data.setHealthScore("67.45");
//        influxService.insertResourceScore(data);
    }

    @RequestMapping("getResourceScore")
    @ResponseBody
    public String getResourceScore() throws JsonProcessingException {
        return mapper.writeValueAsString(influxService.getScoreDataBymonitorAndInterval("11113",7));
    }

    @RequestMapping("calculateBusinessScore")
    @ResponseBody
    public void calculateBusinessScore(String businessId) throws IOException {
        service.calculateBusinessScore(businessId);
    }

    @RequestMapping("getBusinessResourceByMonitorUuid")
    @ResponseBody
    public String getBusinessResourceByMonitorUuid(String monitorUuid) throws JsonProcessingException {
        return mapper.writeValueAsString(service.getBusinessResourceByMonitorUuid(monitorUuid));
    }

    @RequestMapping("getBusinessNode")
    @ResponseBody
    public String getBusinessNode(String uuid) throws JsonProcessingException {
        return mapper.writeValueAsString(service.getBusinessNode(uuid));
    }

    @RequestMapping("getBusinessByPage")
    @ResponseBody
    public String getBusinessListByPage(@RequestBody String data) throws IOException {
        PageData view = mapper.readValue(data, PageData.class);
        return mapper.writeValueAsString(service.getBusinessListByPage(view));
    }

    @RequestMapping("delBusinessResource")
    @ResponseBody
    public boolean delBusinessResource(@RequestBody String data) throws IOException {
        List<BusinessResourceEntity> ress = mapper.readValue(data,new TypeReference<List<BusinessResourceEntity>>(){});
        return service.delBusinessResource(ress);
    }
    @RequestMapping(value = "delBusiness",method =  RequestMethod.DELETE)
    @ResponseBody
    public boolean delBusiness(String uuid) throws IOException {
        return service.delBusiness(uuid);
    }
}
