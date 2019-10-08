package com.chl.gbo.cental.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import com.chl.gbo.cental.domain.Authority;

/**
 * @Auther: BoYanG
 * @Describe 权限
 */
public interface AuthorityRepository extends JpaRepository<Authority, Integer> {

    List<Authority> findByDataUrl(String dataUrl);

    @Modifying
    @Query(value = "delete from tb_role_authority where menuCode =?1", nativeQuery = true)
    void deleteRoleAuth(String code);

    void deleteByMenuCode(String code);

    @Query(value = "SELECT CONCAT(GROUP_CONCAT(menuCode), ',') AS str FROM tb_authority", nativeQuery = true)
    String getConcatMenuCode();


    @Query(value = "SELECT distinct a.sequence, b.menuCode AS pcode, b.menuNAME AS pname, a.menuCode AS code, a.menuName AS name,a.icon,a.dataUrl as url " +
            " FROM tb_authority a LEFT JOIN tb_authority b ON a.parentMenucode = b.menuCode" +
            " LEFT JOIN tb_role_authority c ON a.menuCode = c.menuCode" +
            " WHERE a.menuType = 1 AND c.roleKey IN ?1 ORDER BY a.sequence", nativeQuery = true)
    List<Map> findAuthsByRoleKey(List<String> rolesStr);
}
