package com.chl.gbo.cental.bean;

import java.math.BigInteger;

import org.thymeleaf.util.StringUtils;

import lombok.Data;

/**
 * @Auther: BoYanG
 * @Describe 菜单
 */
@Data
public class MenuDto {

    private BigInteger sequence;

    private String pcode;

    private String pname;

    private String code;

    private String name;

    private String icon;

    private String url;

    public boolean isParent(){
        return StringUtils.isEmpty(getPcode()) && StringUtils.isEmpty(getPname());
    }
}
