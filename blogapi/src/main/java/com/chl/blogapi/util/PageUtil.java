package com.chl.blogapi.util;

import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.google.common.collect.Maps;

/**
 * @Auther: BoYanG
 * @Describe 分页工具
 */
public class PageUtil {

    public static int PAGE_SIZE = 3;

    /**
     * 获取mysql 分页起始
     * @param page
     * @param pageSize
     * @return
     */
    public static int getPageNo(int page,int pageSize){
        return (page-1)*pageSize;
    }

    public static Integer getPageNo(int page){
        return (page>1?(page-1):0)*PAGE_SIZE;
    }

    /**
     * 获取mysql 分页结束,默认往后查十页
     * @param pageSize
     * @return
     */
    public static int getPageLimit(int pageSize){
        return 10 * pageSize;
    }

    /**
     * 获取mysql 真实分页总数
     * @param page
     * @param pageSize
     * @param queryCount
     * @return
     */
    public static int getPageCount(int page,int pageSize,int queryCount){
        return getPageNo(page,pageSize) + queryCount;
    }

    /**
     * 获取mysql 真实分页数据
     * @param dataList
     * @param pageSize
     * @param <T>
     * @return
     */
    public static  <T> List<T> getPageData(List<T> dataList, int pageSize){
        int queryCount = dataList.size();
        int toIndex = pageSize > queryCount ? queryCount : pageSize;
        return dataList.subList(0, toIndex);
    }

    /**
     * 分页信息
     * @param itemCount
     * @param currPage
     * @return
     */
    public static Map getPageInfo(int itemCount, String currPage){
        Map resultPageMap = Maps.newHashMapWithExpectedSize(4);
        resultPageMap.put("currPage", Integer.parseInt(StringUtils.isEmpty(currPage)?"1":currPage));
        resultPageMap.put("pageCount", itemCount % PAGE_SIZE == 0 ? itemCount / PAGE_SIZE:itemCount / PAGE_SIZE + 1);
        resultPageMap.put("pageNumber", PAGE_SIZE);
        resultPageMap.put("itemCount", itemCount);
        return resultPageMap;
    }

    public static int getPageSize(Integer size) {
        return size/PAGE_SIZE+(size%PAGE_SIZE==0?0:1);
    }
}
