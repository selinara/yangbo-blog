package com.chl.gbo.cental.component;

import java.util.Collection;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Component;
import com.chl.gbo.cental.domain.Authority;
import com.chl.gbo.cental.domain.Role;
import com.chl.gbo.cental.service.AuthorityService;
/**
 * @Auther: BoYanG
 * @Describe 接受用户的请地址，返回该地址所需的所有权限
 */
@Component
public class FilterInvocationSecurityMetadataSourceImpl implements FilterInvocationSecurityMetadataSource {

    private static final Logger log = LogManager.getLogger(FilterInvocationSecurityMetadataSourceImpl.class);

    @Autowired
    private AuthorityService authorityService;

    //接收用户请求的地址，返回访问该地址需要的所有权限
    @Override
    public Collection<ConfigAttribute> getAttributes(Object o) throws IllegalArgumentException {
        String url = ((FilterInvocation) o).getRequestUrl();

//        log.info("requestUrl:" + url);

        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }

        //无需权限
        if (url.contains("/login") || url.contains("/error")) {
            return null;
        }

        Authority authority = authorityService.findAthorityByUrl(url);

        if (authority == null) {
            return SecurityConfig.createList("ROLE_LOGIN");
        }

        //将resource所需要到的roles按框架要求封装返回（ResourceService里面的getRoles方法是基于RoleRepository实现的）
        List<Role> roles = authorityService.getRoleListByMenuCode(authority.getMenuCode());
        int size = roles.size();
        String[] values = new String[size];
        for (int i = 0; i < size; i++) {
            values[i] = roles.get(i).getRoleKey();
        }
        return SecurityConfig.createList(values);
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return false;
    }
}
