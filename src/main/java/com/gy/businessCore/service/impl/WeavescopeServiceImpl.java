package com.gy.businessCore.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gy.businessCore.entity.WeaveApi;
import com.gy.businessCore.entity.WeaveContainerImageNodes;
import com.gy.businessCore.service.WeavescopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Created by gy on 2018/12/17.
 */
@Service
public class WeavescopeServiceImpl implements WeavescopeService{

    //http://47.94.157.199:32404/api/topology/containers?system=application&stopped=running&pseudo=hide
    private static final String PATH_API="/api";
    private static final String PATH_CONATINER_IMAGE="/api/topology/containers?stopped=running&pseudo=hide";
    private static final String HTTP = "http://";

    @Autowired
    ObjectMapper mapper;


    @Bean
    public RestTemplate rest(){
        return new RestTemplate();
    }

    @Override
    public WeaveContainerImageNodes getWeaveContainerImage(String url) {
        ResponseEntity<WeaveContainerImageNodes> responseEntity = rest()
                .getForEntity(HTTP+url+PATH_CONATINER_IMAGE,WeaveContainerImageNodes.class);
        return responseEntity.getBody();
    }

    @Override
    public WeaveApi getWeaveApi(String url) {
        ResponseEntity<WeaveApi> responseEntity = rest()
                .getForEntity(HTTP+url+PATH_API,WeaveApi.class);
        return responseEntity.getBody();
    }
}
