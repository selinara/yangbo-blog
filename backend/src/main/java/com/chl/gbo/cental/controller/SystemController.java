package com.chl.gbo.cental.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.chl.gbo.cental.domain.Authority;
import com.chl.gbo.cental.vo.CommonResultVO;
import com.chl.gbo.cental.domain.Role;
import com.chl.gbo.cental.domain.User;
import com.chl.gbo.cental.service.AuthorityService;
import com.chl.gbo.cental.service.RoleService;
import com.chl.gbo.cental.service.UserService;

/**
 * @Auther: BoYanG
 * @Describe 后台系统用户管理
 */
@Controller
@RequestMapping("/system")
public class SystemController {

    private static final Log logger = LogFactory.getLog(SystemController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private AuthorityService authorityService;

    //--------------------------------------------------------用户管理---------------------------------------------------------------
    @GetMapping("/user")
    public String systemUserPage(Model model, String loginAccount){
        model.addAttribute("userList", userService.findAllUsers(loginAccount));
        model.addAttribute("searchUser", loginAccount);
        return "system/system_user";
    }

    @GetMapping("/user/add")
    public String userEdit(Model model){
        model.addAttribute("roles", roleService.findCurrentUserCheckRoleList(null));
        model.addAttribute("tip", "ADD");
        return "system/system_user_aded";
    }

    @GetMapping("/user/edit")
    public String userEdit(String ud, String un, String ids, Model model){
        model.addAttribute("ud", ud);
        model.addAttribute("un", un);
        model.addAttribute("roles", roleService.findCurrentUserCheckRoleList(ids));
        model.addAttribute("tip", "EDIT");
        return "system/system_user_aded";
    }

    @GetMapping("/user/del")
    @ResponseBody
    public String userDel(String ud){
        userService.deleteUserInfoByUd(ud);
        return new CommonResultVO().toGsonResultString();
    }

    @PostMapping("/user/submit")
    public String userSubmit(User user){
        userService.insertUser(user);
        return "redirect:/system/user";
    }

    //--------------------------------------------------------角色管理---------------------------------------------------------------
    @GetMapping("/role")
    public String systemRolePage(Model model, String roleKey){
        model.addAttribute("roleList", roleService.findAllRoles(roleKey));
        return "system/system_role";
    }

    @GetMapping("/role/add")
    public String roleAdd(Model model){
        model.addAttribute("tip", "ADD");
        return "system/system_role_aded";
    }

    @GetMapping("/role/edit")
    public String userRole(String rid, String key, String val, String des, Model model){
        model.addAttribute("rid", rid);
        model.addAttribute("key", key);
        model.addAttribute("val", val);
        model.addAttribute("des", des);
        model.addAttribute("tip", "EDIT");
        return "system/system_role_aded";
    }

    @GetMapping("/role/del")
    @ResponseBody
    public String roleDel(String rid){
        roleService.deleteRoleInfoByRid(rid);
        return new CommonResultVO().toGsonResultString();
    }

    @PostMapping("/role/submit")
    public String roleSubmit(Role role){
        roleService.insertRole(role);
        return "redirect:/system/role";
    }

    /**
     * 角色权限页面
     */
    @GetMapping("/role/gear")
    public String userRole(String rid,String key,  String val, Model model){
        model.addAttribute("rid", rid);
        model.addAttribute("key", key);
        model.addAttribute("val", val);
        model.addAttribute("authPageList", roleService.getAuthPageById(key));
        return "system/system_role_auth";
    }

    @PostMapping("/role/authsubmit")
    public String authsubmit(String roleKey, String menuCode){
        roleService.saveAuthPage(roleKey, menuCode);
        return "redirect:/system/role";
    }

    //--------------------------------------------------------页面权限管理---------------------------------------------------------------
    @GetMapping("/auth")
    public String systemAuthPage(Model model){
        model.addAttribute("authList", authorityService.findAll());
        return "system/system_authority";
    }

    @GetMapping("/auth/add")
    public String authAdd(Model model){
        model.addAttribute("tip", "ADD");
        model.addAttribute("urlArr", authorityService.getConcatMenuCode(null));
        return "system/system_authority_aded";
    }

    @GetMapping("/auth/edit")
    public String authRole(String id,String code , String name, String pcode, String seq, String url,String type,String icon,Model model){
        model.addAttribute("urlArr", authorityService.getConcatMenuCode(code));
        model.addAttribute("id", id);
        model.addAttribute("code", code);
        model.addAttribute("name", name);
        model.addAttribute("icon", icon);
        model.addAttribute("pcode", pcode);
        model.addAttribute("seq", seq);
        model.addAttribute("url", url);
        model.addAttribute("type", type);
        model.addAttribute("tip", "EDIT");
        return "system/system_authority_aded";
    }

    @GetMapping("/auth/del")
    @ResponseBody
    public String authDel(Integer id){
        authorityService.deleteById(id);
        return new CommonResultVO().toGsonResultString();
    }

    @PostMapping("/auth/submit")
    public String authSubmit(Authority authority){
        authorityService.insertAuthority(authority);
        return "redirect:/system/auth";
    }
}
