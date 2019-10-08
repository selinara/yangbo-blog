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
 * @Describe 标签
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "tb_labels")
public class Labels {

    @Id
    @GeneratedValue
    private Integer labelId;

    private String labelName;

    private String labelAlias;

    private String labelDescription;

    @Transient
    private Boolean isCheck = false;
}
