package cl.integrador.busint.woowup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate redisTemplate;

    public boolean validarResource(String clave) {
        if (redisTemplate.opsForValue().get("CLIBUSINT" + clave) != null) {
            return true; // El valor ya existe en Redis
        } else {
            redisTemplate.opsForValue().set("CLIBUSINT" +clave, "Procesando", 5, TimeUnit.MINUTES); // Crear valor con duración de 5 minutos
            return false; // El valor ha sido creado en Redis
        }
    }
}