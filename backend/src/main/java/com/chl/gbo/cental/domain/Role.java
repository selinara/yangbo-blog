package com.chl.gbo.cental.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Auther: BoYanG
 * @Describe role
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "tb_role")
public class Role {
    @Id
    @GeneratedValue
    private Integer roleId;
    private String roleKey;
    private String createTime;
    private String description;
    private String roleValue;
    private Integer companyId;

    @Transient
    private Boolean isCheck = false;
}
