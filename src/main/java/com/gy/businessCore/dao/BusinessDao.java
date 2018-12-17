package com.gy.businessCore.dao;

import com.gy.businessCore.entity.*;

/**
 * Created by gy on 2018/3/31.
 */
public interface BusinessDao {
    public TestEntity getJPAInfo();


    public BusinessEntity insertBusiness(BusinessEntity entity);


}
