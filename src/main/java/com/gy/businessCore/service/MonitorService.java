package com.gy.businessCore.service;

import com.gy.businessCore.entity.BusinessEntity;
import com.gy.businessCore.entity.BusinessResourceEntity;
import com.gy.businessCore.entity.TestEntity;
import com.gy.businessCore.entity.WeaveContainerImageInfo;
import com.gy.businessCore.entity.monitor.*;

import java.util.List;


/**
 * Created by gy on 2018/3/31.
 */
public interface MonitorService {

//    List<OperationMonitorEntity> getAllMonitorRecord();

    /**
     * 获取所有的网络设备 即根据三级规格是一样的
     * @return
     */
    public List<NetworkMonitorEntity> getAllNetworkMonitorEntity();



    /**
     * 获取所有的tomcat
     * @return
     */
    public List<TomcatMonitorEntity> getAllTomcatMonitorEntity();


    /**
     * 获取所有的mysql
     * @return
     */
    public List<DBMonitorEntity> getAllDbMonitorEntity();


    /**
     * 获取所有的cas
     * @return
     */
    public List<CasMonitorEntity> getAllCasMonitorEntity();


    /**
     * 获取所有的cvk
     * @return
     */
    public List<HostMonitorEntity> getAllHostMonitorEntity();


    /**
     * 获取所有的vm
     * @return
     */
    public List<VmMonitorEntity> getAllVmMonitorEntity();


    /**
     * 获取所有的k8s
     * @return
     */
    public List<K8sMonitorEntity> getAllK8sMonitorEntity();



    /**
     * 获取所有的k8snode
     * @return
     */
    public List<K8snodeMonitorEntity> getAllK8snodeMonitorEntity();

    /**
     * 获取所有的k8scontainer
     * @return
     */

    public List<K8scontainerMonitorEntity> getAllK8sContainerMonitorEntity();

    String getQuotaValue(String monitorUuid,String quotaName);
}
