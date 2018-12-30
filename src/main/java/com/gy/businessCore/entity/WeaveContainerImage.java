package com.gy.businessCore.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by gy on 2018/12/17.
 */
@Getter
@Setter
public class WeaveContainerImage {

    private String id;

    private String name;

//    private String label;

//    private String labelMinor;

    private List<String> adjacency;

    private List<WeaveContainerImageTable> tables;


}
