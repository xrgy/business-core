package com.gy.businessCore.service;

import com.gy.businessCore.entity.WeaveApi;
import com.gy.businessCore.entity.WeaveContainerImageNodes;

/**
 * Created by gy on 2018/12/17.
 */
public interface WeavescopeService {

    public WeaveContainerImageNodes getWeaveContainerImage(String url);

    public WeaveApi getWeaveApi(String url);
}
