package com.chl.gbo.cental.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import com.chl.gbo.cental.domain.Role;
import com.chl.gbo.cental.repository.RoleRepository;
import com.chl.gbo.util.DateUtil;

/**
 * @Auther: BoYanG
 * @Describe role
 */
@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public List<Role> getRolesByUserName(String username){
        return roleRepository.getRolesByUserName(username);
    }

    public List<Role> getAuthoritysByMenuCode(String menuCode){
        return roleRepository.getAuthoritysByMenuCode(menuCode);
    }

    public List<Role> findCurrentUserCheckRoleList(String ids) {
        List<Role> roleList = roleRepository.findAll();
        if (!StringUtils.isEmpty(ids)) {
            String[] idArr = ids.split(",");
            for (Role role : roleList) {
                for (String id : idArr) {
                    if (id.equals(String.valueOf(role.getRoleId()))) {
                        role.setIsCheck(true);
                        break;
                    }
                }
            }
        }
        return roleList;
    }

    public List<Role> findAllRoles(String roleKey) {
        if (StringUtils.isEmpty(roleKey))
            return roleRepository.findAll();
        return roleRepository.findByRoleKey(roleKey);
    }

    public void insertRole(Role role) {
        role.setCreateTime(DateUtil.now());
        roleRepository.save(role);
    }


    @Transactional
    public void deleteRoleInfoByRid(String rid) {
        if (StringUtils.isEmpty(rid)) {
            return;
        }

        //user-role
        roleRepository.deleteUserRole(Integer.parseInt(rid));

        //user-auth
        roleRepository.deleteUserAuth(roleRepository.findById(Integer.parseInt(rid)).get().getRoleKey());

        //role
        roleRepository.deleteById(Integer.parseInt(rid));
    }

    public List<Map> getAuthPageById(String key) {
        return roleRepository.getAuthPageById(key);
    }

    @Transactional
    public void saveAuthPage(String roleKey, String menuCode) {
        //clear
        roleRepository.deleteUserAuth(roleKey);

        //save tb_role_authority
        if (!StringUtils.isEmpty(menuCode)) {
            String[] codeArr = menuCode.split(",");
            for (String code : codeArr) {
                roleRepository.insertAuthRole(code, roleKey);
            }
        }
    }
}
