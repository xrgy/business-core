package com.gy.businessCore.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Created by gy on 2018/12/17.
 */
@Getter
@Setter
public class WeaveContainerImageInfo {

    private Map<String, List<WeaveContainerImage>> nodes;
}
