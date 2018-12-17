package com.gy.businessCore.service.impl;


import com.gy.businessCore.dao.BusinessDao;
import com.gy.businessCore.entity.*;
import com.gy.businessCore.service.BusinessService;
import com.gy.businessCore.service.WeavescopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.ls.LSInput;

import java.util.*;


/**
 * Created by gy on 2018/3/31.
 */
@Service
public class BusinessServiceImpl implements BusinessService {

    @Autowired
    BusinessDao dao;

    @Autowired
    WeavescopeService weavescopeService;


    @Override
    public TestEntity getJPAInfo() {
        return dao.getJPAInfo();
    }

    @Override
    public List<Map<String, List<WeaveContainerImage>>> getWeaveContainerImage() {
        String weaveappstr = System.getenv("WEAVE_APP_ENDPOINT");
        List<String> weaveurls = new ArrayList<>();
        if (weaveappstr.contains(";")) {
            weaveurls.addAll(Arrays.asList(weaveappstr.split(";")));
        } else {
            weaveurls.add(weaveappstr);
        }
        List<Map<String, List<WeaveContainerImage>>> results = new ArrayList<>();
        weaveurls.forEach(url -> {
            Map<String, List<WeaveContainerImage>> map = new HashMap<>();
            List<WeaveContainerImage> mapImage = new ArrayList<>();
            WeaveApi api = weavescopeService.getWeaveApi(url);
            WeaveContainerImageNodes imageNodes = weavescopeService.getWeaveContainerImage(url);
            imageNodes.getNodes().forEach((key, value) -> {
                if (key.contains(";") && key.substring(key.lastIndexOf(";")+1, key.length()).equals("<container_image>")) {
                    //去除伪节点 //pseudo:uncontained:master
                    String name = getContainerImageRealName(key);
                    //不能包含weaveworks的，不能包含私有仓库的；labelMinor>10认为是基础镜像 "labelMinor": "16 containers",
                    int minorCount = Integer.parseInt(value.getLabelMinor().split(" ")[0]);
                    if (!name.contains("weaveworks") && !name.contains("docker.io/registry") && minorCount < 10) {
                        String uuid = UUID.randomUUID().toString();
                        BusinessEntity entity = new BusinessEntity();
                        entity.setUuid(uuid);
                        entity.setName(name);
                        dao.insertBusiness(entity);
                        WeaveContainerImage myImage = new WeaveContainerImage();
                        myImage.setId(uuid);
                        myImage.setName(name);
                        List<String> adjs = new ArrayList<>();
                        if (null!=value.getAdjacency()) {
                            value.getAdjacency().forEach(adj -> {
                                if (adj.contains(";")) {
                                    adjs.add(getContainerImageRealName(adj));
                                }
                            });
                            myImage.setAdjacency(adjs);
                        }
                        mapImage.add(myImage);

                    }
                }
            });
            map.put(api.getId(),mapImage);
            results.add(map);
        });
        return results;
    }

    private String getContainerImageRealName(String name) {
        return name.substring(0, name.lastIndexOf(";"));
    }

}
