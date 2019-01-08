package com.gy.businessCore.dao;

import com.gy.businessCore.entity.*;

import java.util.List;

/**
 * Created by gy on 2018/3/31.
 */
public interface BusinessDao {
    public TestEntity getJPAInfo();


    public BusinessEntity insertBusiness(BusinessEntity entity);

    public List getCluster();

    public List<BusinessEntity> getBusinessByCluster(String cluster);

    public BusinessEntity getBusinessByClusterAndImage(String cluster,String image);

    public List<BusinessEntity> getBusinessList();

    boolean insertBusinessResource(BusinessResourceEntity entity);

    /**
     * 根据业务Id获取该业务中的资源
     * @param businessId
     * @return
     */
    List<BusinessResourceEntity> getBusinessResourcesByBusinessId(String businessId);
}
