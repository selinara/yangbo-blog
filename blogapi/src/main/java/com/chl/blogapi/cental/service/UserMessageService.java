package com.chl.blogapi.cental.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chl.blogapi.cental.domain.UserMessage;
import com.chl.blogapi.cental.repository.UserMessageRepository;
import com.chl.blogapi.util.DateUtil;

/**
 * @Auther: BoYanG
 * 留言操作
 */
@Service
public class UserMessageService {

    @Autowired
    private UserMessageRepository userMessageRepository;

    public List<UserMessage> getAll(){
        return userMessageRepository.findAllUserMessageInfo();
    }

    @Transactional
    public int deleteById(String id, String userId) {
        return userMessageRepository.deleteByUserIdAndId(userId, id);
    }

    @Transactional
    public Integer save(Integer userId, String content) {
        return userMessageRepository.saveUserMessage(content, userId);
    }
}
