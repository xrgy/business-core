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
}
