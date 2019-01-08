package com.gy.businessCore.service.impl;


import com.gy.businessCore.common.BusinessEnum;
import com.gy.businessCore.dao.BusinessDao;
import com.gy.businessCore.entity.*;
import com.gy.businessCore.entity.monitor.OperationMonitorEntity;
import com.gy.businessCore.service.BusinessService;
import com.gy.businessCore.service.MonitorService;
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

    @Autowired
    MonitorService monitorService;

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
            List<BusinessEntity> existBusiness = null;
            if (allCluster.contains(api.getId())) {
                existBusiness = dao.getBusinessByCluster(api.getId());
            } else {
                existBusiness = new ArrayList<>();
            }
            WeaveContainerImageNodes imageNodes = weavescopeService.getWeaveContainerImage(url);


            Map<String, String> container2imageId = new HashMap<>();
            Map<String, String> imageId2name = new HashMap<>();
            Map<String, String> containerimageId2uuid = new HashMap<>();
            imageNodes.getNodes().forEach((key, value) -> {
                if (key.contains(";") && key.substring(key.lastIndexOf(";") + 1, key.length()).equals("<container>")) {
                    String contaienrid = key.substring(0, key.lastIndexOf(";"));
                    Optional<WeaveContainerImageTable> tableOpt = value.getTables().stream()
                            .filter(table -> table.getId().equals(BusinessEnum.WeaveEnum.IMAGE_TABLE.value())).findFirst();
                    if (tableOpt.isPresent()) {
                        tableOpt.get().getRows().forEach(x -> {
                            if (x.getId().equals(BusinessEnum.WeaveEnum.IMAGE_ID.value())) {
                                container2imageId.put(contaienrid, x.getEntries().getValue());
                            }
                        });
                        tableOpt.get().getRows().forEach(x -> {
                            if (x.getId().equals(BusinessEnum.WeaveEnum.IMAGE_NAME.value()) && container2imageId.containsKey(contaienrid)) {
                                imageId2name.put(container2imageId.get(contaienrid), x.getEntries().getValue());
                            }
                        });
                    }

                }

            });

            List<BusinessEntity> finalExistBusiness = existBusiness;
            Map<String, List<String>> busuuid2adj = new HashMap<>();
            imageNodes.getNodes().forEach((key, value) -> {
                if (key.contains(";") && key.substring(key.lastIndexOf(";") + 1, key.length()).equals("<container>")) {
                    //去除伪节点 //pseudo:uncontained:master
                    String cid = key.substring(0, key.lastIndexOf(";"));
                    //不能包含weaveworks的，不能包含私有仓库的；labelMinor>10认为是基础镜像 "labelMinor": "16 containers",
//                        int minorCount = Integer.parseInt(value.getLabelMinor().split(" ")[0]);

                    if (container2imageId.containsKey(cid)) {
                        String imid = container2imageId.get(cid);
                        if (imageId2name.containsKey(imid)) {
                            String name = imageId2name.get(imid);
                            if (!name.contains("weaveworks") && !name.contains("docker.io/registry")) {
                                WeaveContainerImage myImage = new WeaveContainerImage();
                                Optional<BusinessEntity> exBusiness = finalExistBusiness.stream()
                                        .filter(x -> x.getImage().equals(imid) && x.getCluster().equals(api.getId())).findFirst();
                                String tmpid = "";
                                if (exBusiness.isPresent()) {
                                    //在数据库中存在，而且不在mapimage中
                                    //不用插入该微服务 但是还是要给拓扑返回去
                                    tmpid = exBusiness.get().getUuid();
                                    myImage.setId(tmpid);
                                } else {
                                    BusinessEntity entity = new BusinessEntity();
                                    tmpid = UUID.randomUUID().toString();
                                    entity.setUuid(tmpid);
                                    entity.setName(name);
                                    entity.setImage(imid);
                                    entity.setCluster(api.getId());
                                    dao.insertBusiness(entity);
                                    myImage.setId(tmpid);
                                    finalExistBusiness.add(entity);
                                }
                                String finalTmpid = tmpid;
                                if (busuuid2adj.containsKey(finalTmpid) && value.getAdjacency() != null && value.getAdjacency().size() > 0) {
                                    List<String> add = busuuid2adj.get(finalTmpid);
                                    add.addAll(value.getAdjacency());
                                    busuuid2adj.put(finalTmpid, add);
                                } else if (!busuuid2adj.containsKey(imid) && value.getAdjacency() != null && value.getAdjacency().size() > 0) {
                                    busuuid2adj.put(finalTmpid, value.getAdjacency());
                                }
                                myImage.setName(name);
//                                myImage.setAdjacency(value.getAdjacency());

                                Optional<WeaveContainerImage> mm = mapImage.stream().filter(x -> x.getId().equals(finalTmpid)).findFirst();
                                if (!mm.isPresent()) {
                                    mapImage.add(myImage);
                                }
                                containerimageId2uuid.put(cid + "-" + imid, finalTmpid);

                            }
                        }
                    }

                }
            });
            mapImage.forEach(image -> {
                if (busuuid2adj.containsKey(image.getId())) {
                    image.setAdjacency(busuuid2adj.get(image.getId()));
                }
            });
            mapImage.forEach(image -> {
                List<String> adjs = new ArrayList<>();
                if (null != image.getAdjacency()) {
                    image.getAdjacency().forEach(adj -> {
                        if (adj.contains(";")) {
                            String conid = getContainerImageRealName(adj);
                            String imnid = container2imageId.get(conid);
                            adjs.add(containerimageId2uuid.get(conid + "-" + imnid));
                        }
                    });
                    image.setAdjacency(adjs);
                }
            });
            map.put(api.getId(), mapImage);
        });
        resultInfo.setNodes(map);
        return resultInfo;
    }

    @Override
    public List<BusinessEntity> getBusinessList() {
        return dao.getBusinessList();
    }

    @Override
    public boolean insertBusinessResource(BusinessResourceEntity resourceEntity) {
        return insertBusinessResource(resourceEntity);
    }

    @Override
    public boolean insertBusinessResourceList(List<BusinessResourceEntity> resourceEntityList) {
        try {
            resourceEntityList.forEach(x -> {
                dao.insertBusinessResource(x);
            });
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    @Override
    public void calculateBusinessScore(String businessId) {
        List<BusinessResourceEntity> businessResourceList = dao.getBusinessResourcesByBusinessId(businessId);
        if (null==businessResourceList || businessResourceList.size()<=0){
            return;
        }
        List<OperationMonitorEntity> monitorList = monitorService.getAllMonitorRecord();
        Map<String,OperationMonitorEntity> monitorUuidList = new HashMap<>();
        monitorList.forEach(x->monitorUuidList.put(x.getUuid(),x));
        //判断业务中的资源是否在监控中，不在则结束 返回 error return掉
        businessResourceList.forEach(x->{
            if (!monitorUuidList.containsKey(x.getMonitorId())){
                //业务中的资源必须全部在监控中，即监控中删除资源时，如果该资源在业务中则不允许删除
                return;
            }
        });

        businessResourceList.forEach(x->{
            OperationMonitorEntity operationMonitorEntity = monitorUuidList.get(x.getMonitorId());
            String monitorType = operationMonitorEntity.getMonitorType();
            if (monitorType.equals(BusinessEnum.MonitorTypeEnum.TOMCAT.value())){

            }else if (monitorType.equals(BusinessEnum.MonitorTypeEnum.MYSQL.value())){

            }else if (monitorType.equals(BusinessEnum.MonitorTypeEnum.CVK.value())){

            }else if (monitorType.equals(BusinessEnum.MonitorTypeEnum.VIRTUALMACHINE.value())){

            }else if (monitorType.equals(BusinessEnum.MonitorTypeEnum.K8SNODE.value())){

            }else if (monitorType.equals(BusinessEnum.MonitorTypeEnum.K8SCONTAINER.value())){

            }
        });

    }

    private String getContainerImageRealName(String name) {
        return name.substring(0, name.lastIndexOf(";"));
    }

}
