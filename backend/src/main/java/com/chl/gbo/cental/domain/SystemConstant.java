package com.chl.gbo.cental.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Auther: BoYanG
 * @Describe 系统常量
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "tb_constant")
public class SystemConstant {
    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    private String keyv;
    private String value;
    private String code;
    private String description;

}
