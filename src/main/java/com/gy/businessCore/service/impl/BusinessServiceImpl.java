package com.gy.businessCore.service.impl;


import com.gy.businessCore.common.BusinessEnum;
import com.gy.businessCore.dao.BusinessDao;
import com.gy.businessCore.entity.*;
import com.gy.businessCore.entity.monitor.*;
import com.gy.businessCore.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.ls.LSInput;

import javax.print.Doc;
import javax.swing.text.html.Option;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.Buffer;
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

    @Autowired
    AlertService alertService;

    @Autowired
    InfluxService influxService;

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
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void calculateBusinessScore(String businessId) throws IOException {
        List<BusinessResourceEntity> businessResourceList = dao.getBusinessResourcesByBusinessId(businessId);
        BusinessEntity businessEntity = dao.getBusinessByUuid(businessId);
        if (null == businessEntity){
            return;
        }
        if (null == businessResourceList || businessResourceList.size() <= 0) {
            return;
        }
        List<OperationMonitorEntity> monitorList = getAllMonitorRecord();
        Map<String, OperationMonitorEntity> monitorUuidList = new HashMap<>();
        monitorList.forEach(x -> monitorUuidList.put(x.getUuid(), x));
        //判断业务中的资源是否在监控中，不在则结束 返回 error return掉
        businessResourceList.forEach(x -> {
            if (!monitorUuidList.containsKey(x.getMonitorId())) {
                //业务中的资源必须全部在监控中，即监控中删除资源时，如果该资源在业务中则不允许删除
                return;
            }
        });
        Map<String, Integer> typeNumMap = new HashMap<>();
        //读变权的参数 a，b
        Properties properties = new Properties();
//        BufferedReader bufferedReader = new BufferedReader(new FileReader("C:/Users/gy/IdeaProjects/business-core/src/main/resources/config/busyweight.properties"));
        BufferedReader bufferedReader = new BufferedReader(new FileReader("/busyweight.properties"));
        properties.load(bufferedReader);
        Double variableA = str2double(properties.getProperty(BusinessEnum.VariableWeightParamEnum.VARIABLE_A.value()));
        Double variableB = str2double(properties.getProperty(BusinessEnum.VariableWeightParamEnum.VARIABLE_B.value()));
        Map<String, Double> resourceBusyScoreMap = new HashMap<>();
        //计算资源的繁忙度
        businessResourceList.forEach(x -> {
            OperationMonitorEntity operationMonitorEntity = monitorUuidList.get(x.getMonitorId());
            String monitorType = operationMonitorEntity.getLightType();
            double resBusyScore = 0;
            //计算出每个资源的繁忙度
            if (monitorType.equals(BusinessEnum.LightTypeEnum.TOMCAT.value())) {
                List<Double> tomcatConstantW = new ArrayList<>();
                tomcatConstantW.add(str2double(properties.getProperty(BusinessEnum.BusyWeightTypeEnum.TOMCAT.value() + "." +
                        BusinessEnum.BusyQuotaEnum.PROCESSING_PERSEC.value())));
                tomcatConstantW.add(str2double(properties.getProperty(BusinessEnum.BusyWeightTypeEnum.TOMCAT.value() + "." +
                        BusinessEnum.BusyQuotaEnum.THREAD_BUSY_PERCENT.value())));
                String persec = monitorService.getQuotaValue(x.getMonitorId(), BusinessEnum.QuotaEnum.TOMCAT_PROCESSINGPERSEC.value());
                if (null==persec){
                    persec="0";
                }
                String threadBusyPercent = monitorService.getQuotaValue(x.getMonitorId(), BusinessEnum.QuotaEnum.TOMCAT_THREADSBUSYPERCENT.value());
                if (null==threadBusyPercent){
                    threadBusyPercent="0";
                }
                List<Double> tomcatQuotaValue = new ArrayList<>();
                int min = str2int(properties.getProperty(BusinessEnum.BusyWeightTypeEnum.TOMCAT.value() + "." +
                        BusinessEnum.BusyQuotaEnum.PROCESSING_PERSEC.value() + ".min"));
                int max = str2int(properties.getProperty(BusinessEnum.BusyWeightTypeEnum.TOMCAT.value() + "." +
                        BusinessEnum.BusyQuotaEnum.PROCESSING_PERSEC.value() + ".max"));
                tomcatQuotaValue.add(valueNormaliz(min, max, str2double(persec)));
                tomcatQuotaValue.add(str2double(threadBusyPercent));
                resBusyScore = calResourceBusyScore(tomcatConstantW, tomcatQuotaValue, variableA, variableB);
                //resBusyScore就是这个tomcat资源的繁忙度
            } else if (monitorType.equals(BusinessEnum.LightTypeEnum.MYSQL.value())) {
                String questionRate = monitorService.getQuotaValue(x.getMonitorId(), BusinessEnum.QuotaEnum.MYSQL_QUESTIONSRATE.value());
                if (null==questionRate){
                    questionRate="0";
                }
                resBusyScore = str2double(questionRate);
                //resBusyScore就是这个mysql资源的繁忙度
            } else if (monitorType.equals(BusinessEnum.LightTypeEnum.CVK.value())) {
                List<Double> cvkConstantW = new ArrayList<>();
                cvkConstantW.add(str2double(properties.getProperty(BusinessEnum.BusyWeightTypeEnum.CVK.value() + "." +
                        BusinessEnum.BusyQuotaEnum.CPU_PERCENT.value())));
                cvkConstantW.add(str2double(properties.getProperty(BusinessEnum.BusyWeightTypeEnum.CVK.value() + "." +
                        BusinessEnum.BusyQuotaEnum.MEMORY_PERCENT.value())));
                String cvkCpuRate = monitorService.getQuotaValue(x.getMonitorId(), BusinessEnum.QuotaEnum.CVK_CPU_USAGE.value());
                if (null==cvkCpuRate){
                    cvkCpuRate="0";
                }
                String cvkMemRate = monitorService.getQuotaValue(x.getMonitorId(), BusinessEnum.QuotaEnum.CVK_MEM_USAGE.value());
                if (null==cvkMemRate){
                    cvkMemRate="0";
                }
                List<Double> cvkQuotaValue = new ArrayList<>();
                cvkQuotaValue.add(str2double(cvkCpuRate));
                cvkQuotaValue.add(str2double(cvkMemRate));
                resBusyScore = calResourceBusyScore(cvkConstantW, cvkQuotaValue, variableA, variableB);
                //resBusyScore就是这个cvk的资源繁忙度
            } else if (monitorType.equals(BusinessEnum.LightTypeEnum.VIRTUALMACHINE.value())) {
                List<Double> vmConstantW = new ArrayList<>();
                vmConstantW.add(str2double(properties.getProperty(BusinessEnum.BusyWeightTypeEnum.VIRTUALMACHINE.value() + "." +
                        BusinessEnum.BusyQuotaEnum.CPU_PERCENT.value())));
                vmConstantW.add(str2double(properties.getProperty(BusinessEnum.BusyWeightTypeEnum.VIRTUALMACHINE.value() + "." +
                        BusinessEnum.BusyQuotaEnum.MEMORY_PERCENT.value())));
                String vmCpuRate = monitorService.getQuotaValue(x.getMonitorId(), BusinessEnum.QuotaEnum.VM_CPU_USAGE.value());
                if (null==vmCpuRate){
                    vmCpuRate="0";
                }
                String vmMemRate = monitorService.getQuotaValue(x.getMonitorId(), BusinessEnum.QuotaEnum.VM_MEM_USAGE.value());
                if (null==vmMemRate){
                    vmMemRate="0";
                }
                List<Double> vmQuotaValue = new ArrayList<>();
                vmQuotaValue.add(str2double(vmCpuRate));
                vmQuotaValue.add(str2double(vmMemRate));
                resBusyScore = calResourceBusyScore(vmConstantW, vmQuotaValue, variableA, variableB);
                //resBusyScore就是这个vm的繁忙度

            } else if (monitorType.equals(BusinessEnum.LightTypeEnum.K8SNODE.value())) {
                String k8snCpuRate = monitorService.getQuotaValue(x.getMonitorId(), BusinessEnum.QuotaEnum.K8SNODE_CPU_USAGE.value());
                if (null==k8snCpuRate){
                    k8snCpuRate="0";
                }
                String k8snMemRate = monitorService.getQuotaValue(x.getMonitorId(), BusinessEnum.QuotaEnum.K8SNODE_MEM_USAGE.value());
                if (null==k8snMemRate){
                    k8snMemRate="0";
                }
                List<Double> k8snConstantW = new ArrayList<>();
                k8snConstantW.add(str2double(properties.getProperty(BusinessEnum.BusyWeightTypeEnum.K8SNODE.value() + "." +
                        BusinessEnum.BusyQuotaEnum.CPU_PERCENT.value())));
                k8snConstantW.add(str2double(properties.getProperty(BusinessEnum.BusyWeightTypeEnum.K8SNODE.value() + "." +
                        BusinessEnum.BusyQuotaEnum.MEMORY_PERCENT.value())));
                List<Double> k8snQuotaValue = new ArrayList<>();
                k8snQuotaValue.add(str2double(k8snCpuRate));
                k8snQuotaValue.add(str2double(k8snMemRate));
                resBusyScore = calResourceBusyScore(k8snConstantW, k8snQuotaValue, variableA, variableB);
                //resBusyScore就是这个k8snode的繁忙度

            } else if (monitorType.equals(BusinessEnum.LightTypeEnum.K8SCONTAINER.value())) {
                String k8scCpuRate = monitorService.getQuotaValue(x.getMonitorId(), BusinessEnum.QuotaEnum.K8SCONTAINER_CPU_USAGE.value());
                if (null==k8scCpuRate){
                    k8scCpuRate="0";
                }
                String k8scMemRate = monitorService.getQuotaValue(x.getMonitorId(), BusinessEnum.QuotaEnum.K8SCONTAINER_MEM_USAGE.value());
                if (null==k8scMemRate){
                    k8scMemRate="0";
                }
                List<Double> k8scConstantW = new ArrayList<>();
                k8scConstantW.add(str2double(properties.getProperty(BusinessEnum.BusyWeightTypeEnum.K8SCONTAINER.value() + "." +
                        BusinessEnum.BusyQuotaEnum.CPU_PERCENT.value())));
                k8scConstantW.add(str2double(properties.getProperty(BusinessEnum.BusyWeightTypeEnum.K8SCONTAINER.value() + "." +
                        BusinessEnum.BusyQuotaEnum.MEMORY_PERCENT.value())));
                List<Double> k8scQuotaValue = new ArrayList<>();
                k8scQuotaValue.add(str2double(k8scCpuRate));
                k8scQuotaValue.add(str2double(k8scMemRate));
                resBusyScore = calResourceBusyScore(k8scConstantW, k8scQuotaValue, variableA, variableB);
                //resBusyScore就是这个k8scontainer的繁忙度
            }
            resourceBusyScoreMap.put(x.getMonitorId(), resBusyScore);
            if (typeNumMap.containsKey(monitorType)) {
                int num = typeNumMap.get(monitorType);
                typeNumMap.put(monitorType, num + 1);
            } else {
                typeNumMap.put(monitorType, 1);
            }
        });
        //计算业务繁忙度
        Map<String, Double> resWeightMap = null;
        Map<String, Double> typeWeighMap = new HashMap<>();
        if (typeNumMap.containsKey(BusinessEnum.MonitorTypeEnum.MYSQL.value()) && typeNumMap.containsKey(BusinessEnum.MonitorTypeEnum.TOMCAT.value())) {
            //包含tomcat和mysql 情况四 situationWithBoth
            Double k8snWe4 = str2double(properties.getProperty(BusinessEnum.ResourceWeightEnum.SITUATION_WITH_BOTH.value() + "." +
                    BusinessEnum.BusyWeightTypeEnum.K8SNODE.value()));
            Double k8scWe4 = str2double(properties.getProperty(BusinessEnum.ResourceWeightEnum.SITUATION_WITH_BOTH.value() + "." +
                    BusinessEnum.BusyWeightTypeEnum.K8SCONTAINER.value()));
            Double cvkWe4 = str2double(properties.getProperty(BusinessEnum.ResourceWeightEnum.SITUATION_WITH_BOTH.value() + "." +
                    BusinessEnum.BusyWeightTypeEnum.CVK.value()));
            Double vmWe4 = str2double(properties.getProperty(BusinessEnum.ResourceWeightEnum.SITUATION_WITH_BOTH.value() + "." +
                    BusinessEnum.BusyWeightTypeEnum.VIRTUALMACHINE.value()));
            Double tomWe4 = str2double(properties.getProperty(BusinessEnum.ResourceWeightEnum.SITUATION_WITH_BOTH.value() + "." +
                    BusinessEnum.BusyWeightTypeEnum.TOMCAT.value()));
            Double sqlWe4 = str2double(properties.getProperty(BusinessEnum.ResourceWeightEnum.SITUATION_WITH_BOTH.value() + "." +
                    BusinessEnum.BusyWeightTypeEnum.MYSQL.value()));
            typeWeighMap.put(BusinessEnum.MonitorTypeEnum.K8SNODE.value(), k8snWe4);
            typeWeighMap.put(BusinessEnum.MonitorTypeEnum.K8SCONTAINER.value(), k8scWe4);
            typeWeighMap.put(BusinessEnum.MonitorTypeEnum.CVK.value(), cvkWe4);
            typeWeighMap.put(BusinessEnum.MonitorTypeEnum.VIRTUALMACHINE.value(), vmWe4);
            typeWeighMap.put(BusinessEnum.MonitorTypeEnum.TOMCAT.value(), tomWe4);
            typeWeighMap.put(BusinessEnum.MonitorTypeEnum.MYSQL.value(), sqlWe4);
        } else if (typeNumMap.containsKey(BusinessEnum.MonitorTypeEnum.MYSQL.value())) {
            //只包含mysql 情况二 situationWithMySQL
            Double k8snWe2 = str2double(properties.getProperty(BusinessEnum.ResourceWeightEnum.SITUATION_WITH_MYSQL.value() + "." +
                    BusinessEnum.BusyWeightTypeEnum.K8SNODE.value()));
            Double k8scWe2 = str2double(properties.getProperty(BusinessEnum.ResourceWeightEnum.SITUATION_WITH_MYSQL.value() + "." +
                    BusinessEnum.BusyWeightTypeEnum.K8SCONTAINER.value()));
            Double cvkWe2 = str2double(properties.getProperty(BusinessEnum.ResourceWeightEnum.SITUATION_WITH_MYSQL.value() + "." +
                    BusinessEnum.BusyWeightTypeEnum.CVK.value()));
            Double vmWe2 = str2double(properties.getProperty(BusinessEnum.ResourceWeightEnum.SITUATION_WITH_MYSQL.value() + "." +
                    BusinessEnum.BusyWeightTypeEnum.VIRTUALMACHINE.value()));
            Double sqlWe2 = str2double(properties.getProperty(BusinessEnum.ResourceWeightEnum.SITUATION_WITH_MYSQL.value() + "." +
                    BusinessEnum.BusyWeightTypeEnum.MYSQL.value()));
            typeWeighMap.put(BusinessEnum.MonitorTypeEnum.K8SNODE.value(), k8snWe2);
            typeWeighMap.put(BusinessEnum.MonitorTypeEnum.K8SCONTAINER.value(), k8scWe2);
            typeWeighMap.put(BusinessEnum.MonitorTypeEnum.CVK.value(), cvkWe2);
            typeWeighMap.put(BusinessEnum.MonitorTypeEnum.VIRTUALMACHINE.value(), vmWe2);
            typeWeighMap.put(BusinessEnum.MonitorTypeEnum.MYSQL.value(), sqlWe2);
        } else if (typeNumMap.containsKey(BusinessEnum.MonitorTypeEnum.TOMCAT.value())) {
            //只包含tomcat 情况三 situationWithTomcat
            Double k8snWe2 = str2double(properties.getProperty(BusinessEnum.ResourceWeightEnum.SITUATION__WITH_TOMCAT.value() + "." +
                    BusinessEnum.BusyWeightTypeEnum.K8SNODE.value()));
            Double k8scWe2 = str2double(properties.getProperty(BusinessEnum.ResourceWeightEnum.SITUATION__WITH_TOMCAT.value() + "." +
                    BusinessEnum.BusyWeightTypeEnum.K8SCONTAINER.value()));
            Double cvkWe2 = str2double(properties.getProperty(BusinessEnum.ResourceWeightEnum.SITUATION__WITH_TOMCAT.value() + "." +
                    BusinessEnum.BusyWeightTypeEnum.CVK.value()));
            Double vmWe2 = str2double(properties.getProperty(BusinessEnum.ResourceWeightEnum.SITUATION__WITH_TOMCAT.value() + "." +
                    BusinessEnum.BusyWeightTypeEnum.VIRTUALMACHINE.value()));
            Double tomWe2 = str2double(properties.getProperty(BusinessEnum.ResourceWeightEnum.SITUATION__WITH_TOMCAT.value() + "." +
                    BusinessEnum.BusyWeightTypeEnum.TOMCAT.value()));
            typeWeighMap.put(BusinessEnum.MonitorTypeEnum.K8SNODE.value(), k8snWe2);
            typeWeighMap.put(BusinessEnum.MonitorTypeEnum.K8SCONTAINER.value(), k8scWe2);
            typeWeighMap.put(BusinessEnum.MonitorTypeEnum.CVK.value(), cvkWe2);
            typeWeighMap.put(BusinessEnum.MonitorTypeEnum.VIRTUALMACHINE.value(), vmWe2);
            typeWeighMap.put(BusinessEnum.MonitorTypeEnum.TOMCAT.value(), tomWe2);
        } else {
            //情况一 situationOne 只有 虚拟化和k8s
            Double k8snWe2 = str2double(properties.getProperty(BusinessEnum.ResourceWeightEnum.SITUATION_ONE.value() + "." +
                    BusinessEnum.BusyWeightTypeEnum.K8SNODE.value()));
            Double k8scWe2 = str2double(properties.getProperty(BusinessEnum.ResourceWeightEnum.SITUATION_ONE.value() + "." +
                    BusinessEnum.BusyWeightTypeEnum.K8SCONTAINER.value()));
            Double cvkWe2 = str2double(properties.getProperty(BusinessEnum.ResourceWeightEnum.SITUATION_ONE.value() + "." +
                    BusinessEnum.BusyWeightTypeEnum.CVK.value()));
            Double vmWe2 = str2double(properties.getProperty(BusinessEnum.ResourceWeightEnum.SITUATION_ONE.value() + "." +
                    BusinessEnum.BusyWeightTypeEnum.VIRTUALMACHINE.value()));
            typeWeighMap.put(BusinessEnum.MonitorTypeEnum.K8SNODE.value(), k8snWe2);
            typeWeighMap.put(BusinessEnum.MonitorTypeEnum.K8SCONTAINER.value(), k8scWe2);
            typeWeighMap.put(BusinessEnum.MonitorTypeEnum.CVK.value(), cvkWe2);
            typeWeighMap.put(BusinessEnum.MonitorTypeEnum.VIRTUALMACHINE.value(), vmWe2);
        }
        resWeightMap = calResWeight(businessResourceList, monitorUuidList, typeWeighMap);
        //业务繁忙度
        Double busyscore = calBussinessBusyScore(resourceBusyScoreMap, resWeightMap);
        businessEntity.setBusy_score(double2float2(busyscore));
        //业务健康度
        //先获取资源的monitorstauts 如果为0则该资源的健康度为0
        Properties propertiesHealth = new Properties();
//        BufferedReader bufferedReaderHealth = new BufferedReader(new FileReader("C:/Users/gy/IdeaProjects/business-core/src/main/resources/config/healthRatio.properties"));
        BufferedReader bufferedReaderHealth = new BufferedReader(new FileReader("/healthRatio.properties"));
        propertiesHealth.load(bufferedReaderHealth);
        Double criticalRatio = str2double(propertiesHealth.getProperty(BusinessEnum.AlertTypeEnum.CRITICAL.value()));
        Double majorRatio = str2double(propertiesHealth.getProperty(BusinessEnum.AlertTypeEnum.MAJOR.value()));
        Double minorRatio = str2double(propertiesHealth.getProperty(BusinessEnum.AlertTypeEnum.MINOR.value()));
        Double warningRatio = str2double(propertiesHealth.getProperty(BusinessEnum.AlertTypeEnum.WARNING.value()));
        Double noticeRatio = str2double(propertiesHealth.getProperty(BusinessEnum.AlertTypeEnum.NOTICE.value()));
        List<Double> ratioList = new ArrayList<>();
        ratioList.add(criticalRatio);
        ratioList.add(majorRatio);
        ratioList.add(minorRatio);
        ratioList.add(warningRatio);
        ratioList.add(noticeRatio);
        //资源健康度
        Map<String, Double> resHealthMap = new HashMap<>();

        businessResourceList.forEach(x -> {
            OperationMonitorEntity operationMonitorEntity = monitorUuidList.get(x.getMonitorId());
            String monitorType = operationMonitorEntity.getMonitorType();
            String monitorStatusQuotaName = "";
            if (monitorType.equals(BusinessEnum.MonitorTypeEnum.TOMCAT.value())) {
                monitorStatusQuotaName = BusinessEnum.QuotaEnum.TOMCAT_MONITORSTATUS.value();
            } else if (monitorType.equals(BusinessEnum.MonitorTypeEnum.MYSQL.value())) {
                monitorStatusQuotaName = BusinessEnum.QuotaEnum.MYSQL_MONITORSTATUS.value();
            } else if (monitorType.equals(BusinessEnum.MonitorTypeEnum.CVK.value())) {
                monitorStatusQuotaName = BusinessEnum.QuotaEnum.CVK_MONITORSTATUS.value();
            } else if (monitorType.equals(BusinessEnum.MonitorTypeEnum.VIRTUALMACHINE.value())) {
                monitorStatusQuotaName = BusinessEnum.QuotaEnum.VIRTUALMACHINE_MONITORSTATUS.value();
            } else if (monitorType.equals(BusinessEnum.MonitorTypeEnum.K8SNODE.value())) {
                monitorStatusQuotaName = BusinessEnum.QuotaEnum.K8SNODE_MONITORSTATUS.value();
            } else if (monitorType.equals(BusinessEnum.MonitorTypeEnum.K8SCONTAINER.value())) {
                monitorStatusQuotaName = BusinessEnum.QuotaEnum.K8SCONTAINER_MONITORSTATUS.value();
            }
            String monitorstatus = monitorService.getQuotaValue(x.getMonitorId(), monitorStatusQuotaName);
            if (monitorstatus.equals("0")) {
                //监控不可达 则这个资源的健康度为0
                resHealthMap.put(x.getMonitorId(), 0.0);
            } else {
                //监控可达，则根据告警进行资源健康度
                Map<String, Integer> severityCount = alertService.getAlertInfoByMonitorUuid(x.getMonitorId());
                Double resHealthScore = calResourceHealthScore(ratioList, severityCount);
                resHealthMap.put(x.getMonitorId(), resHealthScore);
            }
        });
        //业务健康度
        Double healthscore = calBussinessBusyScore(resHealthMap, resWeightMap);
        businessEntity.setHealth_score(double2float2(healthscore));
        //资源可用度
        Map<String,Double> resAvailableMap = new HashMap<>();
        int day = str2int(propertiesHealth.getProperty(BusinessEnum.BusinessAvailableEnum.AVAILABLE_INTERVAL.value()));
        businessResourceList.forEach(x -> {
            Double resAvaliable = 0.0;
            Double reHealth = resHealthMap.get(x.getMonitorId());
            List<InfluxData> influxDataList = influxService.getScoreDataBymonitorAndInterval(x.getMonitorId(), day);
            if (null == influxDataList || influxDataList.size() == 0){
                //如果列表为空，判断健康度是否为0，为0，则可用性为0，健康度不为0，可用性为100.0，
                if (reHealth == 0){
                    resAvaliable = 0.0;
                }else {
                    resAvaliable = 100.0;
                }
            }else {
                //如果列表不为空，如果现在这次健康度为0，则（1-（7天内健康度为0的次数+1）/（7天内所有数据条数+1））*100，
                // 如果这次健康度不为0，则（1-（7天内健康度为0的次数）/（7天内所有数据条数+1））*100
                int healthCount = calHealthCount(influxDataList);
                int sumcount  = influxDataList.size();
                if (reHealth==0){
                    resAvaliable = (1-(healthCount+1)*1.0/(sumcount+1))*100;
                }else {
                    resAvaliable = (1-healthCount*1.0/(sumcount+1))*100;
                }

            }
            resAvailableMap.put(x.getMonitorId(),resAvaliable);
        });
        Double availablescore = calBussinessBusyScore(resAvailableMap, resWeightMap);
        businessEntity.setAvailable_score(double2float2(availablescore));
        //将资源的健康度，繁忙度，可用度保存至数据库 influxdb,mysql
        businessResourceList.forEach(x->{
            double busy = double2float2(resourceBusyScoreMap.get(x.getMonitorId()));
            double health = double2float2(resHealthMap.get(x.getMonitorId()));
            double ava = double2float2(resAvailableMap.get(x.getMonitorId()));
            x.setBusy_score(busy);
            x.setHealth_score(health);
            x.setAvailable_score(ava);
            dao.insertBusinessResource(x);
            InfluxData data = new InfluxData();
            data.setSource_id(x.getMonitorId());
            data.setBusy_score(busy);
            data.setHealth_score(health);
            data.setAvailable_score(ava);
            influxService.insertResourceScore(data);
        });
        //将业务的健康度，繁忙度，可用度保存在mysql
        dao.insertBusiness(businessEntity);
    }

    @Override
    public List<BusinessResourceEntity> getBusinessResourcesByBusinessId(String businessId) {
        return dao.getBusinessResourcesByBusinessId(businessId);
    }

    @Override
    public List<BusinessResourceEntity> getBusinessResourceByMonitorUuid(String monitorUuid) {
        return dao.getBusinessResourceByMonitorUuid(monitorUuid);
    }

    @Override
    public BusinessEntity getBusinessNode(String uuid) {
        return dao.getBusinessByUuid(uuid);
    }

    @Override
    public PageBean getBusinessListByPage(PageData view) {
        List<BusinessEntity> list = dao.getBusinessList();
        PageBean pageBean = new PageBean(view.getPageIndex(),view.getPageSize(),list.size());
        List<BusinessEntity> mylist = dao.getBusinessListByPage(pageBean.getStartIndex(),view.getPageSize());
        pageBean.setList(mylist);
        return pageBean;
    }

    @Override
    public boolean delBusinessResource(List<BusinessResourceEntity> ress) {
        try {
            ress.forEach(x -> {
                dao.delBusinessResource(x);
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private List<OperationMonitorEntity> getAllMonitorRecord() {
        List<OperationMonitorEntity> operationMonitorEntities = new ArrayList<>();
        List<DBMonitorEntity> dblist = monitorService.getAllDbMonitorEntity();
        dblist.forEach(x->{
            OperationMonitorEntity o = new OperationMonitorEntity();
            BeanUtils.copyProperties(x,o);
            o.setLightType(BusinessEnum.LightTypeEnum.MYSQL.value());
            operationMonitorEntities.add(o);
        });
        List<TomcatMonitorEntity> tomcatList = monitorService.getAllTomcatMonitorEntity();
        tomcatList.forEach(x->{
            OperationMonitorEntity o = new OperationMonitorEntity();
            BeanUtils.copyProperties(x,o);
            o.setLightType(BusinessEnum.LightTypeEnum.TOMCAT.value());
            operationMonitorEntities.add(o);
        });
        List<HostMonitorEntity> cvkList = monitorService.getAllHostMonitorEntity();
        cvkList.forEach(x->{
            OperationMonitorEntity o = new OperationMonitorEntity();
            BeanUtils.copyProperties(x,o);
            o.setLightType(BusinessEnum.LightTypeEnum.CVK.value());
            operationMonitorEntities.add(o);
        });
        List<VmMonitorEntity> vmList = monitorService.getAllVmMonitorEntity();
        vmList.forEach(x->{
            OperationMonitorEntity o = new OperationMonitorEntity();
            BeanUtils.copyProperties(x,o);
            o.setLightType(BusinessEnum.LightTypeEnum.VIRTUALMACHINE.value());
            operationMonitorEntities.add(o);
        });
        List<K8snodeMonitorEntity> k8snlist = monitorService.getAllK8snodeMonitorEntity();
        k8snlist.forEach(x->{
            OperationMonitorEntity o = new OperationMonitorEntity();
            BeanUtils.copyProperties(x,o);
            o.setLightType(BusinessEnum.LightTypeEnum.K8SNODE.value());
            operationMonitorEntities.add(o);
        });
        List<K8scontainerMonitorEntity> k8scList = monitorService.getAllK8sContainerMonitorEntity();
        k8scList.forEach(x->{
            OperationMonitorEntity o = new OperationMonitorEntity();
            BeanUtils.copyProperties(x,o);
            o.setLightType(BusinessEnum.LightTypeEnum.K8SCONTAINER.value());
            operationMonitorEntities.add(o);
        });
        return operationMonitorEntities;
    }

    /**
     * 将double转为float保留两位小数
     * @param d
     * @return
     */
    private double double2float2(Double d) {
        BigDecimal b = new BigDecimal(d);
//        float df = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
        double df = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

        return df;
    }

    /**
     * 计算influxdb这些数据中健康度为0的次数
     * @param influxDataList
     * @return
     */
    private int calHealthCount(List<InfluxData> influxDataList) {
        int count = 0;
        for (InfluxData influxData : influxDataList) {
            if (influxData.getHealth_score() == 0){
                count++;
            }
        }
        return count;

    }

    /**
     * 计算资源健康度
     *
     * @param ratioList     告警严重系数列表
     * @param severityCount 告警级别 数量 map
     * @return
     */
    private Double calResourceHealthScore(List<Double> ratioList, Map<String, Integer> severityCount) {
        Double sum = 0.0;
        for (int i = 0; i < ratioList.size(); i++) {
            Integer cpunt =0;
            if (severityCount.containsKey(i+"")) {
                cpunt = severityCount.get(i + "");
            }
            sum += cpunt * ratioList.get(i);
        }
        Double score = 100 - sum;
        if (score < 0) {
            return 0.0;
        } else {
            return score;
        }
    }

    /**
     * 计算业务繁忙度
     *
     * @param resourceBusyScoreMap monitorId 资源繁忙度Map
     * @param resWeightMap         monitorId 资源权重Map
     * @return
     */
    private Double calBussinessBusyScore(Map<String, Double> resourceBusyScoreMap, Map<String, Double> resWeightMap) {
        Double score = 0.0;
        for (Map.Entry<String, Double> m : resWeightMap.entrySet()) {
            score += m.getValue() * resourceBusyScoreMap.get(m.getKey());
        }
        return score;
    }


    /**
     * 计算各资源权重
     *
     * @param businessResourceList 业务资源列表
     * @param monitorUuidList      监控列表
     * @param typeWeighMap         类型权重map
     * @return
     */
    private Map<String, Double> calResWeight(List<BusinessResourceEntity> businessResourceList, Map<String,
            OperationMonitorEntity> monitorUuidList, Map<String, Double> typeWeighMap) {
        Double sum = 0.0;
        Map<String, Double> monitorWeight = new HashMap<>();
        for (int i = 0; i < businessResourceList.size(); i++) {
            BusinessResourceEntity x = businessResourceList.get(i);
            OperationMonitorEntity operationMonitorEntity = monitorUuidList.get(x.getMonitorId());
            String monitorType = operationMonitorEntity.getMonitorType();
            sum += typeWeighMap.get(monitorType);
        }
        Double finalSum = sum;
        businessResourceList.forEach(x -> {
            OperationMonitorEntity operationMonitorEntity = monitorUuidList.get(x.getMonitorId());
            String monitorType = operationMonitorEntity.getMonitorType();
            monitorWeight.put(x.getMonitorId(), typeWeighMap.get(monitorType) / finalSum);
        });
        return monitorWeight;
    }

    /**
     * 计算资源繁忙度
     *
     * @param constantW  常权列表
     * @param quotaValue 指标值列表
     * @param variableA  变权参数a
     * @param variableB  变权参数b
     * @return
     */
    private Double calResourceBusyScore(List<Double> constantW, List<Double> quotaValue, Double variableA, Double variableB) {
        List<Double> wb = verifyWeight(constantW, quotaValue, variableA, variableB);
        double reBusy = 0;
        for (int i = 0; i < quotaValue.size(); i++) {
            reBusy += quotaValue.get(i) * wb.get(i);
        }
        return reBusy;
    }

    private Double str2double(String str) {
        Double d = Double.parseDouble(str);
        return d;
    }

    private int str2int(String str) {
        int d = Integer.parseInt(str);
        return d;
    }

    /**
     * 指标归一化
     *
     * @param min     指标最小值
     * @param max     指标最小值
     * @param current 指标值
     * @return
     */
    private double valueNormaliz(int min, int max, double current) {
        return (current - min) / (max - min);
    }

    /**
     * 计算变权
     *
     * @param constantWeight 常权列表
     * @param quotaValue     指标值列表
     * @param a              参数a
     * @param b              参数b
     * @return
     */
    private List<Double> verifyWeight(List<Double> constantWeight, List<Double> quotaValue, Double a, Double b) {

        List<Double> si = new ArrayList<>();
        for (int i = 0; i < quotaValue.size(); i++) {
            if (quotaValue.get(i) <= 85) {
                si.add(1.0);
            } else {
                double p = a * (quotaValue.get(i) - 85) * 1.0 / 100;
                double s = Math.pow(Math.E, p);
                si.add(s);
            }
        }
        List<Double> wj = new ArrayList<>();
        double sum = 0;
        for (int i = 0; i < constantWeight.size(); i++) {
            sum += constantWeight.get(i) * si.get(i);
        }
        if (sum > 0) {
            for (int i = 0; i < constantWeight.size(); i++) {
                double w = constantWeight.get(i) * si.get(i);
                wj.add(w / sum);
            }
        }
        return wj;
    }

    private String getContainerImageRealName(String name) {
        return name.substring(0, name.lastIndexOf(";"));
    }

}
