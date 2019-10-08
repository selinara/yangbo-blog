package com.chl.gbo.cental.service;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chl.gbo.cental.domain.Authority;
import com.chl.gbo.cental.domain.Role;
import com.chl.gbo.cental.repository.AuthorityRepository;
import com.chl.gbo.util.DateUtil;
import com.google.common.collect.Lists;

/**
 * @Auther: BoYanG
 * @Describe TODO
 */
@Service
public class AuthorityService {

    @Autowired
    private AuthorityRepository authorityRepository;
    @Autowired
    private RoleService roleService;

    public Authority findAthorityByUrl(String dataUrl){
        List<Authority> result = authorityRepository.findByDataUrl(dataUrl);
        if (null != result && result.size() != 0) {
            return result.get(0);
        }
        return null;
    }

    public List<Role> getRoleListByMenuCode(String menuCode){
        return roleService.getAuthoritysByMenuCode(menuCode);
    }

    public List<Authority> findAll() {
        Sort sort = new Sort(Sort.Direction.ASC, "sequence");
        return authorityRepository.findAll(sort);
    }

    public void deleteAuthInfoByCode(String code) {
        if (StringUtils.isEmpty(code)) {
            return;
        }

        //role-authority
        authorityRepository.deleteRoleAuth(code);

        //role
        authorityRepository.deleteByMenuCode(code);
    }

    public void insertAuthority(Authority authority) {
        authority.setCreateTime(DateUtil.now());
        authorityRepository.save(authority);
    }

    public void deleteById(Integer id) {
        authorityRepository.deleteById(id);
    }

    public String getConcatMenuCode(String menuCode) {
        String menuArr = authorityRepository.getConcatMenuCode();
        if (menuCode == null) {
            return menuArr;
        }
        menuArr = menuArr.replace(menuCode+",","");
        return menuArr;
    }

    /**
     * 根据用户信息加载权限菜单
     * @param authentication
     * @return
     */
    public JSONArray getMenusByUserAuthor(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        List<String> roleList = Lists.newArrayList();
        while (iterator.hasNext()){
            roleList.add(iterator.next().getAuthority());
        }
        List<Map> auths = authorityRepository.findAuthsByRoleKey(roleList);
        return toJSONByAuthList(auths);
    }

    private JSONArray toJSONByAuthList(List<Map> auths) {
        JSONArray resultArray = new JSONArray();
        Iterator<Map> iter = auths.iterator();
        // 一级菜单
        while (iter.hasNext()) {
            JSONObject result = new JSONObject();
            Map menu = iter.next();
            if (menu.get("pcode") ==null && menu.get("pname") == null) {
                result.put("code", menu.get("code"));
                result.put("name", menu.get("name"));
                result.put("icon", menu.get("icon"));
                resultArray.add(result);
                iter.remove();
            }
        }

        // 二级菜单
        for (Object o : resultArray) {
            JSONObject obj = (JSONObject) o;
            JSONArray sonArray = new JSONArray();
            Iterator<Map> it = auths.iterator();
            while (it.hasNext()) {
                Map s = it.next();
                if (s.get("pcode").equals(obj.getString("code"))) {
                    JSONObject os = new JSONObject();
                    os.put("code", s.get("code"));
                    os.put("name", s.get("name"));
                    os.put("url", s.get("url"));
                    sonArray.add(os);
                }
            }
            if(sonArray.size() > 0){
                obj.put("submenu", sonArray);
            }
        }
        return resultArray;
    }
}
