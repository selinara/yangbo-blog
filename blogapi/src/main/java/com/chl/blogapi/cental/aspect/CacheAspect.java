package com.chl.blogapi.cental.aspect;

import java.lang.annotation.Annotation;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.chl.blogapi.cental.aspect.annotation.Cache;
import com.chl.blogapi.cental.aspect.annotation.CacheKey;
import com.chl.blogapi.cental.aspect.annotation.CacheObj;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

@Aspect
@Component
public class CacheAspect implements InitializingBean {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final Log logger = LogFactory.getLog(CacheAspect.class);

    @Pointcut("@annotation(com.chl.blogapi.cental.aspect.annotation.Cache)")
    private void point(){}

    @Around("point()")
    public Object before(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Cache cache = signature.getMethod().getAnnotation(Cache.class);
        String prefix = cache.prefix();
        StringBuilder cacheKey = new StringBuilder(prefix).append(":");
        Object[] args = joinPoint.getArgs();
        Annotation[][] parameterAnnotations = signature.getMethod().getParameterAnnotations();
        for (Annotation[] parameterAnnotation : parameterAnnotations) {
            int paramIndex = ArrayUtils.indexOf(parameterAnnotations, parameterAnnotation);
            for (Annotation annotation : parameterAnnotation) {
                if (annotation instanceof CacheKey){
                    Optional<Object> optional = Optional.fromNullable(args[paramIndex]);
                    if (!optional.isPresent())
                        return joinPoint.proceed();
                    cacheKey.append(optional.get()).append("_");
                }
                if (annotation instanceof CacheObj){
                    Optional<Object> optional = Optional.fromNullable(args[paramIndex]);
                    if (!optional.isPresent())
                        return joinPoint.proceed();
                    String path = ((CacheObj) annotation).path();
                    Object read = JSONPath.read(JSONObject.toJSONString(optional.get()), path);
                    if (read == null)
                        return joinPoint.proceed();
                    cacheKey.append(read).append("_");
                }
            }
        }
        if (Objects.equal(cacheKey,new StringBuilder(prefix).append(":"))){
            return joinPoint.proceed();
        }
        Class returnType = ((MethodSignature) joinPoint.getSignature()).getReturnType();
        cacheKey.append("cache");
        String cacheValue = redisTemplate.opsForValue().get(cacheKey.toString());
        if (StringUtils.isNotEmpty(cacheValue)){
//            logger.info(String.format("Get Cache value %s by key %s", cacheValue, cacheKey.toString()));
            return JSON.parseObject(cacheValue,returnType);
        }
        Object result = joinPoint.proceed();
        if (result != null){
            cacheValue = JSON.toJSONString(result,SerializerFeature.WRITE_MAP_NULL_FEATURES, SerializerFeature.WriteDateUseDateFormat,SerializerFeature.QuoteFieldNames,SerializerFeature.WriteClassName);
            redisTemplate.opsForValue().set(cacheKey.toString(),cacheValue ,cache.time(), cache.unit());
//            logger.info(String.format("Cache value %s by key %s", JSONObject.toJSONString(result), cacheKey.toString()));
        }
        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 配置支持fastjson autoType 白名单
        ParserConfig.getGlobalInstance().addAccept("com.chl.blogapi.cental.bean.");
        ParserConfig.getGlobalInstance().addAccept("com.chl.blogapi.cental.config.");
        ParserConfig.getGlobalInstance().addAccept("com.chl.blogapi.cental.domain.");
        ParserConfig.getGlobalInstance().addAccept("com.chl.blogapi.cental.vo.");
    }

}
