package com.chl.blogapi.cental.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.chl.blogapi.cental.domain.SystemConstant;

/**
 * @Auther: BoYanG
 * @Describe TODO
 */
public interface SystemConstantRepository extends JpaRepository<SystemConstant, Integer> {

    @Query(value = "SELECT * FROM tb_constant WHERE  keyv = ?1", nativeQuery = true)
    List<SystemConstant> getObjByKey(String key);
}
