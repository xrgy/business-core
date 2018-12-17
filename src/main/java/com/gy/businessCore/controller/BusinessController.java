package com.gy.businessCore.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gy.businessCore.entity.TestEntity;
import com.gy.businessCore.entity.WeaveContainerImage;
import com.gy.businessCore.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
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

}
