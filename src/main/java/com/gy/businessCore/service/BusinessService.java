package com.gy.businessCore.service;

import com.gy.businessCore.entity.*;

import java.util.List;
import java.util.Map;


/**
 * Created by gy on 2018/3/31.
 */
public interface BusinessService {
    public TestEntity getJPAInfo();


    public List<Map<String,List<WeaveContainerImage>>> getWeaveContainerImage();

}
