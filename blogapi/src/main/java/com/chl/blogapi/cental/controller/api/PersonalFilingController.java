package com.chl.blogapi.cental.controller.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chl.blogapi.cental.enumclass.BaseError;
import com.chl.blogapi.cental.service.ArticleService;
import com.chl.blogapi.cental.vo.CommonResultVO;
import com.chl.blogapi.cental.vo.PersonFilingVO;
import com.chl.blogapi.util.DateUtil;
import com.chl.blogapi.util.HttpUtil;
import com.chl.blogapi.util.SimpleRateLimiter;

/**
 * @Auther: BoYanG
 * @Describe 个人归档
 */
@RestController
@RequestMapping(value = "/api")
public class PersonalFilingController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private SimpleRateLimiter personFilingLimiter = new SimpleRateLimiter("pesonal_ip_limit_60", "ip_limit_60",60, 3600 * 3);

    @RequestMapping(value = "/filing", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String filing(@RequestParam("callback") String cb,
                         HttpServletRequest request){
        CommonResultVO cr = new CommonResultVO();

        String ip = HttpUtil.getIpAddr(request);

        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(ip)) {
            if (!personFilingLimiter.acquire(ip, stringRedisTemplate)) {
                cr.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return cr.toGsonResultString(cb);
            }
        }

        List<Map> pfList = articleService.getPersonFilingList();
        List<String> yearList = transferYears(pfList);
        List<PersonFilingVO> result = new ArrayList<>();
        for (String year: yearList) {
            PersonFilingVO pf = new PersonFilingVO();
            pf.setArticleYear(year);
            List<Map> pList = new ArrayList<>();
            for (Map article : pfList) {
                String m = article.get("mon").toString();
                String month = DateUtil.toPartten(m, "MM月dd日");
                if(DateUtil.toPartten(m, "yyyy").equals(year)){
                    pList.add(article);
                }
            }
            pf.setPfList(pList);
            result.add(pf);
        }
        cr.put("filing", result);
        return cr.toGsonResultString(cb);
    }

    private List<String> transferYears(List<Map> pfList) {
        List<String> result = new ArrayList<>();
        for (Map map : pfList) {
            String year = DateUtil.toPartten(map.get("mon").toString(), "yyyy");
            if (!result.contains(year)) {
                result.add(year);
            }
        }
        return result;
    }
}
