package com.chl.gbo.cental.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import com.chl.gbo.cental.domain.Labels;
import com.chl.gbo.cental.repository.ArticleRepository;
import com.chl.gbo.cental.repository.LabelRepository;
import com.chl.gbo.cental.threadpool.SynaOperationUtil;

/**
 * @Auther: BoYanG
 * @Describe TODO
 */
@Service
public class LabelService {

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public List<Labels> getAll(){
        return labelRepository.findAll();
    }

    public Object findLabelsListBySelected(String ids) {
        List<Labels> labels = getAll();
        if (!StringUtils.isEmpty(ids)) {
            String[] idArr = ids.split(",");
            for (Labels labeler : labels) {
                for (String id : idArr) {
                    if (id.equals(String.valueOf(labeler.getLabelId()))) {
                        labeler.setIsCheck(true);
                        break;
                    }
                }
            }
        }
        return labels;
    }

    @Transactional
    public void deleteLabelById(Integer lid) {
        labelRepository.deleteById(lid);
        articleRepository.deleteArticleByLableId("%,"+String.valueOf(lid)+",%");
        SynaOperationUtil.deleteCacheByArticleId(null, stringRedisTemplate, true);
    }

    public void insertLabel(Labels labels) {
        labelRepository.save(labels);
    }
}
