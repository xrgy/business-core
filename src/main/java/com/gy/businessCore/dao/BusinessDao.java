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

}
