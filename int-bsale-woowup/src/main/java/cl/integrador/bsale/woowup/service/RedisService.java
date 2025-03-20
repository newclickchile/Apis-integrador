package cl.integrador.bsale.woowup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate redisTemplate;

    public boolean validarResource(String clave) {
        if (redisTemplate.opsForValue().get(clave) != null) {
            return true; // El valor ya existe en Redis
        } else {
            redisTemplate.opsForValue().set(clave, "Procesando", 5, TimeUnit.MINUTES); // Crear valor con duración de 5 minutos
            return false; // El valor ha sido creado en Redis
        }
    }
    public String buscaSku(String clave) {
        return redisTemplate.opsForValue().get(clave).toString();
    }
    public boolean insertSku(String clave, String valor) {
        redisTemplate.opsForValue().set(clave, valor);
        return true;
    }
}