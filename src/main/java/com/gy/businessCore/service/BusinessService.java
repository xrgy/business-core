package com.gy.businessCore.service;

import com.gy.businessCore.entity.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * Created by gy on 2018/3/31.
 */
public interface BusinessService {
    public TestEntity getJPAInfo();


    public WeaveContainerImageInfo getWeaveContainerImage();


    public List<BusinessEntity> getBusinessList();

    /**
     * 添加业务资源
     * @param resourceEntity
     * @return
     */
    boolean insertBusinessResource(BusinessResourceEntity resourceEntity);

    /**
     * 添加业务资源列表
     * @param resourceEntityList
     * @return
     */
    boolean insertBusinessResourceList(List<BusinessResourceEntity> resourceEntityList);


    /**
     * 计算业务score
     * @param businessId
     */
    void calculateBusinessScore(String businessId) throws IOException;

    /**
     * 根据业务Id获取该业务中的资源
     * @param businessId
     * @return
     */
    List<BusinessResourceEntity> getBusinessResourcesByBusinessId(String businessId);
    /**
     * 根据资源uuid获取业务资源实体
     * @param monitorUuid
     * @return
     */
    List<BusinessResourceEntity> getBusinessResourceByMonitorUuid(String monitorUuid);

    /**
     * 通过业务id获取业务信息
     * @param uuid
     * @return
     */
    BusinessEntity getBusinessNode(String uuid);

    /**
     * 分页获取business 数据
     * @param view
     * @return
     */
    PageBean getBusinessListByPage(PageData view);

    boolean delBusinessResource(List<BusinessResourceEntity> ress);
}
