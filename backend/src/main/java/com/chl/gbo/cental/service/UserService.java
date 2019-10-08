package com.chl.gbo.cental.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.thymeleaf.util.StringUtils;
import com.chl.gbo.cental.bean.UserDetailsDto;
import com.chl.gbo.cental.domain.User;
import com.chl.gbo.cental.repository.UserRepository;
import com.chl.gbo.util.BCryptUtil;
import com.chl.gbo.util.DateUtil;

/**
 * @Auther: BoYanG
 * @Describe 框架需要使用到一个实现了UserDetailsService接口的类
 */

@Service
public class UserService implements UserDetailsService {

    private static final Logger log = LogManager.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Transactional
    public List<User> findAllUsers(String loginAccount){
        if (StringUtils.isEmpty(loginAccount)){
            loginAccount="";
        } else {
            loginAccount = "%"+loginAccount+"%";
        }
        return userRepository.findAllUsersAndAuthority(loginAccount);
    }

    @Transactional
    public List<User> findUserByUsername(String username){
        return userRepository.findByLoginAccount(username);
    }

    //根据用户名 返回一个UserDetails的实现类的实例
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info(String.format("find user by username,username=%s", username));
        List<User> users = findUserByUsername(username);
        if (users==null || users.size() == 0) {
            throw new UsernameNotFoundException("Not Exist User");
        }
        User user = users.get(0);
        // 封装成UserDetails返回
        return new UserDetailsDto(user, roleService.getRolesByUserName(user.getLoginAccount()));
    }

    @Transactional
    public void insertUser(User user) {
        if (user.getUserId() != null) {
            userRepository.deleteUserRole(user.getUserId());
        }

        //user
        user.setLoginPass(BCryptUtil.encode(user.getLoginPass()));
        user.setRegisterTime(DateUtil.now());
        userRepository.save(user);

        //user-role
        String roleIds = user.getRoleIds();
        String[] ids = roleIds.split(",");
        for (String id : ids) {
            userRepository.addUserRole(user.getUserId(), Integer.parseInt(id));
        }
    }

    @Transactional
    public void deleteUserInfoByUd(String ud) {
        if (StringUtils.isEmpty(ud)) {
            return;
        }

        //user-role
        userRepository.deleteUserRole(Integer.parseInt(ud));

        //user
        userRepository.deleteById(Integer.parseInt(ud));
    }

}
