package com.chl.gbo.cental.controller;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.alibaba.fastjson.JSON;
import com.chl.gbo.cental.bean.DatatableRequest;
import com.chl.gbo.cental.bean.DatatablesView;
import com.chl.gbo.cental.bean.UserDetailsDto;
import com.chl.gbo.cental.domain.Article;
import com.chl.gbo.cental.vo.CommonResultVO;
import com.chl.gbo.cental.domain.Labels;
import com.chl.gbo.cental.domain.Sorts;
import com.chl.gbo.cental.service.ArticleService;
import com.chl.gbo.cental.service.LabelService;
import com.chl.gbo.cental.service.SortService;

/**
 * @Auther: BoYanG
 * @Describe 博文管理
 */
@Controller
@RequestMapping(value = "/article")
public class ArticleController {

    private static Log logger = LogFactory.getLog(ArticleController.class);

    @Autowired
    private ArticleService articleService;
    @Autowired
    private SortService sortService;
    @Autowired
    private LabelService labelService;

    //----------------------------------------------------------博文管理----------------------------------------------------
    @GetMapping(value = "/index")
    public String indexGet(Model model){
        model.addAttribute("sorts", sortService.getAll());
        model.addAttribute("labels", labelService.getAll());
        return "article/article_list";
    }

    @PostMapping(value = "/index", produces = "text/json;charset=UTF-8")
    @ResponseBody
    public String indexPost(
            @RequestParam(value = "articleTitle") String articleTitle,
            @RequestParam(value = "sortId") Integer sortId,
            @RequestParam(value = "labelId") String labelId,
                        HttpServletRequest request, Authentication authentication){
        Integer userId = UserDetailsDto.getCurrentUserId(authentication);
        DatatablesView<Article> result = new DatatablesView<>(request);
        result.setData(articleService.getArticlePage(userId, articleTitle,sortId,labelId,
                DatatableRequest.start(request), DatatableRequest.length(request)));
        result.setRecordsTotal(articleService.count(articleTitle, userId, sortId, labelId));
        return JSON.toJSONString(result);
    }

    @GetMapping("/add")
    public String articleAdd(Model model){
        model.addAttribute("article", new Article());
        model.addAttribute("sorts", sortService.getAll());
        model.addAttribute("labels", labelService.getAll());
        model.addAttribute("tip", "新增博文");
        return "article/article_aded";
    }

    @GetMapping("/edit")
    public String articleEdit(String aid, Model model){
        Article article = articleService.findById(Integer.parseInt(aid));
        model.addAttribute("article", article);
        model.addAttribute("sorts", sortService.findSortsListBySelected(article.getSortId()));
        model.addAttribute("labels", labelService.findLabelsListBySelected(article.getLabelId()));
        model.addAttribute("tip", "博文编辑");
        return "article/article_aded";
    }

    @GetMapping("/del")
    @ResponseBody
    public String userDel(String aid){
        articleService.deleteById(Integer.parseInt(aid));
        return new CommonResultVO().toGsonResultString();
    }

    @GetMapping("/status")
    public String status(Integer aid, Integer st){
        articleService.status(aid, st);
        return "redirect:/article/index";
    }

    @GetMapping("/roof")
    public String roof(Integer aid){
        articleService.roof(aid);
        return "redirect:/article/index";
    }

    @PostMapping("/submit")
    public String userSubmit(Article article, Authentication authentication){
        article.setUserId(UserDetailsDto.getCurrentUserId(authentication));
        article.setLabelId(","+article.getLabelId()+",");
        articleService.insert(article);
        return "redirect:/article/index";
    }

    //----------------------------------------------------------博文分类管理----------------------------------------------------
    @GetMapping("/sort")
    public String sort(Model model){
        model.addAttribute("sortList", sortService.getAll());
        return "article/sort_list";
    }

    @GetMapping("/sort/add")
    public String sortAdd(Model model){
        model.addAttribute("tip", "分类编辑");
        return "article/sort_aded";
    }

    @GetMapping("/sort/edit")
    public String sortEdit(String sid, String name, String alias, String des, Model model){
        model.addAttribute("sid", sid);
        model.addAttribute("name", name);
        model.addAttribute("alias", alias);
        model.addAttribute("des", des);
        model.addAttribute("tip", "分类编辑");
        return "article/sort_aded";
    }

    @GetMapping("/sort/del")
    @ResponseBody
    public String sortDel(Integer sid){
        sortService.deleteSortById(sid);
        return new CommonResultVO().toGsonResultString();
    }

    @PostMapping("/sort/submit")
    public String sortSubmit(Sorts sort){
        sortService.insertSort(sort);
        return "redirect:/article/sort";
    }

    //----------------------------------------------------------博文标签管理----------------------------------------------------
    @GetMapping("/label")
    public String label(Model model){
        model.addAttribute("labelList", labelService.getAll());
        return "article/label_list";
    }

    @GetMapping("/label/add")
    public String labelAdd(Model model){
        model.addAttribute("tip", "添加标签");
        return "article/label_aded";
    }

    @GetMapping("/label/edit")
    public String labelEdit(String lid, String name, String alias, String des, Model model){
        model.addAttribute("lid", lid);
        model.addAttribute("name", name);
        model.addAttribute("alias", alias);
        model.addAttribute("des", des);
        model.addAttribute("tip", "标签编辑");
        return "article/label_aded";
    }

    @GetMapping("/label/del")
    @ResponseBody
    public String labelDel(Integer lid){
        labelService.deleteLabelById(lid);
        return new CommonResultVO().toGsonResultString();
    }

    @PostMapping("/label/submit")
    public String labelSubmit(Labels labels){
        labelService.insertLabel(labels);
        return "redirect:/article/label";
    }

}
