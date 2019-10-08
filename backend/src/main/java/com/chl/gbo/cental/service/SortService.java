package com.chl.gbo.cental.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chl.gbo.cental.domain.Sorts;
import com.chl.gbo.cental.repository.ArticleRepository;
import com.chl.gbo.cental.repository.SortRepository;
import com.chl.gbo.cental.threadpool.SynaOperationUtil;

/**
 * @Auther: BoYanG
 * @Describe 博文分类
 */
@Service
public class SortService {

    @Autowired
    private SortRepository sortRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public List<Sorts> getAll(){
        return sortRepository.findAll();
    }

    public List<Sorts> findSortsListBySelected(Integer sortId) {
        List<Sorts> sorts = getAll();
        for (Sorts sort : sorts) {
            if (sort.getSortId().equals(sortId)) {
                sort.setIsSelect(1);
                break;
            }
        }
        return sorts;
    }

    @Transactional
    public void deleteSortById(Integer sid) {
        sortRepository.deleteById(sid);
        articleRepository.deleteBySortId(sid);
        SynaOperationUtil.deleteCacheByArticleId(null, redisTemplate, true);
    }

    public void insertSort(Sorts sort) {
        sortRepository.save(sort);
    }
}
