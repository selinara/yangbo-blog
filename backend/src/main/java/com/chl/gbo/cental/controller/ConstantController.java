package com.chl.gbo.cental.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.chl.gbo.cental.domain.SystemConstant;
import com.chl.gbo.cental.service.SystemConstantService;
import com.chl.gbo.cental.vo.CommonResultVO;

/**
 * @Auther: BoYanG
 * @Describe 系统常量
 */
@Controller
@RequestMapping("/system")
public class ConstantController {

    @Autowired
    private SystemConstantService systemConstantService;

    @GetMapping("/constant")
    public String constant(Model model){
        model.addAttribute("constantList", systemConstantService.getAll());
        return "system/system_constant";
    }

    @GetMapping("/constant/add")
    public String constantAdd(Model model){
        model.addAttribute("tip", "添加常量");
        return "system/system_constant_aded";
    }

    @GetMapping("/constant/edit")
    public String constantEdit(String id, String name, String keyv, String value, String code, String des, Model model){
        model.addAttribute("id", id);
        model.addAttribute("name", name);
        model.addAttribute("keyv", keyv);
        model.addAttribute("value", value);
        model.addAttribute("code", code);
        model.addAttribute("des", des);
        model.addAttribute("tip", "常量编辑");
        return "system/system_constant_aded";
    }

    @GetMapping("/constant/del")
    @ResponseBody
    public String constantDel(Integer lid){
        systemConstantService.deleteConstantById(lid);
        return new CommonResultVO().toGsonResultString();
    }

    @PostMapping("/constant/submit")
    public String constantSubmit(SystemConstant constants){
        systemConstantService.insert(constants);
        return "redirect:/system/constant";
    }
}
