package com.mars.deltaforce.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DfResponseWithDataList {

    private Integer code;
    private String msg;
    private List<DfData> data;

}
