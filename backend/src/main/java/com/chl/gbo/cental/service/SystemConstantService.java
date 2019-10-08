package com.chl.gbo.cental.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.chl.gbo.cental.domain.SystemConstant;
import com.chl.gbo.cental.repository.SystemConstantRepository;
import com.chl.gbo.cental.threadpool.SynaOperationUtil;

/**
 * @Auther: BoYanG
 * @Describe TODO
 */
@Service
public class SystemConstantService {

    @Autowired
    private SystemConstantRepository systemConstantRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public List<SystemConstant> getAll(){
        return systemConstantRepository.findAll();
    }

    public void deleteConstantById(Integer id) {
        SynaOperationUtil.cleanFriendShipCache(redisTemplate);
        systemConstantRepository.deleteById(id);
    }

    public void insert(SystemConstant constants) {
        SynaOperationUtil.cleanFriendShipCache(redisTemplate);
        systemConstantRepository.save(constants);
    }

    public SystemConstant getSystemConstantByKey(String key) {
        List<SystemConstant> reList = systemConstantRepository.getObjByKey(key);
        if (reList!=null && reList.size()>0) {
            return reList.get(0);
        }
        return null;
    }
}
