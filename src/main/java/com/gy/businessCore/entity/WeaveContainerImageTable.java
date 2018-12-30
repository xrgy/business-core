package com.gy.businessCore.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by gy on 2018/12/29.
 */
@Getter
@Setter
public class WeaveContainerImageTable {

    private String id;

    private List<WeaveContainerImageTableRows> rows;
}
