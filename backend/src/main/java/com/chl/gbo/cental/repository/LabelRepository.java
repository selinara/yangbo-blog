package com.chl.gbo.cental.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.chl.gbo.cental.domain.Labels;

/**
 * @Auther: BoYanG
 * @Describe TODO
 */
public interface LabelRepository extends JpaRepository<Labels, Integer> {

    @Query(value = "SELECT a.* FROM tb_labels a LEFT JOIN " +
            "(SELECT  b.labelId,COUNT(DISTINCT a.articleId) AS n  FROM tb_articles a,tb_labels b WHERE a.labelId " +
            "LIKE CONCAT('%,',CONCAT(b.labelId, ',%'))  GROUP BY b.labelId ) b ON a.labelId =b.labelId ORDER BY b.n DESC  LIMIT 10", nativeQuery = true)
    List<Labels> getHoteTags();
}
