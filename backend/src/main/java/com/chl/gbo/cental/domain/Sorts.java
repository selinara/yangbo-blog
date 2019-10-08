package com.chl.gbo.cental.domain;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Auther: BoYanG
 * @Describe 博文分类
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "tb_sorts")
public class Sorts {

    @Id
    @GeneratedValue
    private Integer sortId;

    private String sortName;

    private String sortAlias;

    private String sortDescription;

    @Transient
    private Integer isSelect = 0;// 选中1，未选中0
}
