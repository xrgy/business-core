package com.gy.businessCore.service.impl;


import com.gy.businessCore.dao.BusinessDao;
import com.gy.businessCore.entity.*;
import com.gy.businessCore.service.BusinessService;
import com.gy.businessCore.service.WeavescopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.ls.LSInput;

import javax.swing.text.html.Option;
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
    public WeaveContainerImageInfo getWeaveContainerImage() {
        List<String> allCluster = dao.getCluster();
        String weaveappstr = System.getenv("WEAVE_APP_ENDPOINT");
        List<String> weaveurls = new ArrayList<>();
        if (weaveappstr.contains(";")) {
            weaveurls.addAll(Arrays.asList(weaveappstr.split(";")));
        } else {
            weaveurls.add(weaveappstr);
        }
//        List<Map<String, List<WeaveContainerImage>>> results = new ArrayList<>();
        WeaveContainerImageInfo resultInfo = new WeaveContainerImageInfo();
        Map<String, List<WeaveContainerImage>> map = new HashMap<>();
        weaveurls.forEach(url -> {
            List<WeaveContainerImage> mapImage = new ArrayList<>();
            WeaveApi api = weavescopeService.getWeaveApi(url);
            if (allCluster.contains(api.getId())) {
                List<BusinessEntity> existBusiness = dao.getBusinessByCluster(api.getId());
                List<String> existBusinessName = new ArrayList<>();
                existBusiness.forEach(x -> existBusinessName.add(x.getName()));
                WeaveContainerImageNodes imageNodes = weavescopeService.getWeaveContainerImage(url);
                imageNodes.getNodes().forEach((key, value) -> {
                    if (key.contains(";") && key.substring(key.lastIndexOf(";") + 1, key.length()).equals("<container_image>")) {
                        String name = getContainerImageRealName(key);
                        //不能包含weaveworks的，不能包含私有仓库的；labelMinor>10认为是基础镜像 "labelMinor": "16 containers",
                        int minorCount = Integer.parseInt(value.getLabelMinor().split(" ")[0]);
                        if (!name.contains("weaveworks") && !name.contains("docker.io/registry") && minorCount < 10) {
                            WeaveContainerImage myImage = new WeaveContainerImage();
                            Optional<BusinessEntity> exBusiness = existBusiness.stream()
                                    .filter(x -> x.getName().equals(name) && x.getCluster().equals(api.getId())).findFirst();

                            if (exBusiness.isPresent()) {
                                //不用插入该微服务 但是还是要给拓扑返回去
                                myImage.setId(exBusiness.get().getUuid());
                            } else {
                                String buuid = UUID.randomUUID().toString();
                                BusinessEntity entity = new BusinessEntity();
                                entity.setUuid(buuid);
                                entity.setName(name);
                                entity.setCluster(api.getId());
                                dao.insertBusiness(entity);
                                myImage.setId(buuid);
                            }
                            myImage.setName(name);
                            List<String> adjs = new ArrayList<>();
                            if (null != value.getAdjacency()) {
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
            } else {
                WeaveContainerImageNodes imageNodes = weavescopeService.getWeaveContainerImage(url);
                imageNodes.getNodes().forEach((key, value) -> {
                    if (key.contains(";") && key.substring(key.lastIndexOf(";") + 1, key.length()).equals("<container_image>")) {
                        //去除伪节点 //pseudo:uncontained:master
                        String name = getContainerImageRealName(key);
                        //不能包含weaveworks的，不能包含私有仓库的；labelMinor>10认为是基础镜像 "labelMinor": "16 containers",
                        int minorCount = Integer.parseInt(value.getLabelMinor().split(" ")[0]);
                        if (!name.contains("weaveworks") && !name.contains("docker.io/registry") && minorCount < 10) {
                            String uuid = UUID.randomUUID().toString();
                            BusinessEntity entity = new BusinessEntity();
                            entity.setUuid(uuid);
                            entity.setName(name);
                            entity.setCluster(api.getId());
                            dao.insertBusiness(entity);
                            WeaveContainerImage myImage = new WeaveContainerImage();
                            myImage.setId(uuid);
                            myImage.setName(name);
                            List<String> adjs = new ArrayList<>();
                            if (null != value.getAdjacency()) {
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
            }
            map.put(api.getId(), mapImage);
        });
        resultInfo.setNodes(map);
        return resultInfo;
    }

    private String getContainerImageRealName(String name) {
        return name.substring(0, name.lastIndexOf(";"));
    }

}
