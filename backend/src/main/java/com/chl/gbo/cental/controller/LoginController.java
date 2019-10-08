package com.chl.gbo.cental.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.chl.gbo.cental.service.AuthorityService;

/**
 * @Auther: BoYanG
 * @Describe 登录-首页
 */
@Controller
public class LoginController {

    private static final Log  logger = LogFactory.getLog(LoginController.class);

    @Autowired
    private AuthorityService authorityService;

    @GetMapping(value = "/login")
    public String login(){
        logger.info("=====================后台登录==============");
        return "login";
    }

    @GetMapping(value = "/home")
    public String homePage(){
        logger.info("=====================主页================");
        return "home_page";
    }

    @RequestMapping(value = "/index")
    public String index(Model model,Authentication authentication){
        logger.info(authentication.getName()+" has login backend!!!!");
        model.addAttribute("currentUser", authentication.getName());
        model.addAttribute("authoritys", authorityService.getMenusByUserAuthor(authentication));
        return "index";
    }

    @GetMapping(value = "/no/permission")
    public String nopermission(){
        return "no_permission";
    }

    @GetMapping(value = "/error")
    public String error(){
        return "login";
    }

    @GetMapping(value = "/logout")
    public String logout(){
        return "login";
    }

}
