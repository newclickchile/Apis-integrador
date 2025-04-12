package cl.integrador.bsale.woowup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate redisTemplate;


    public String getValue(String clave) {
        Object sku = redisTemplate.opsForValue().get(clave);
        return null == sku
                ?null
                :(String) sku ;
    }

}