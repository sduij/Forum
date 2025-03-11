package com.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtils {
    @Value("${spring.security.jwt.key}")
    String key;
    @Value("${spring.security.jwt.expire}")
    int expire;
    @Resource
    StringRedisTemplate template;

    /*JWT去头，非空，算法校验有效性并解析，再取UUID，再存redis*/
    public boolean invalidateJwt(String headerToken){
        String token=this.convertToken(headerToken);
        if (token == null) {
            return false;
        }
        Algorithm algorithm=Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT jwt = jwtVerifier.verify(token);

            String id=jwt.getId();
            return deleteToken(id,jwt.getExpiresAt());

        } catch (JWTVerificationException e) {  /*运行时异常*/
            return false;
        }
    }

    /*先判断JWT失效没，没失效就存到redis返回true*/
    public boolean deleteToken(String uuid,Date time){
        if (this.isInvalidToken(uuid)) {
            return false;/*失效返回false*/
        }

        Date now = new Date();
        long expire = Math.max(time.getTime() - now.getTime(), 0);/*和0比较，小于0就取0*/
        template.opsForValue().set( Const.JWT_BLACK_LIST + uuid,  "", expire, TimeUnit.MILLISECONDS);
        return true;
    }
    /*验证某个JWT是否已经被标记为无效*/
    public boolean isInvalidToken(String uuid){
        return template.hasKey(Const.JWT_BLACK_LIST+uuid);
    }

    /*解析JWT*/
    public DecodedJWT resolveJWT(String headerToken){
        String token=this.convertToken(headerToken);
        if (token == null) {
            return null;
        }
        Algorithm algorithm=Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();/*使用指定的算法创建一个 JWTVerifier 实例，用于验证JWT的有效性*/
        try {
            DecodedJWT verify = jwtVerifier.verify(token);/*使用 jwtVerifier 验证提供的 token。如果验证成功，则返回解码后的JWT (DecodedJWT)*/
            /*判断JWT是否在redis里*/
            if (this.isInvalidToken(verify.getId())) {
                return null;
            }
            /*判断JWT是否过期*/
            Date expiresAt = verify.getExpiresAt();/*JWT过期时间*/
            return new Date().after(expiresAt) ? null : verify;/*通过比较当前时间和JWT的过期时间(expiresAt)来判断*/
        } catch (JWTVerificationException e) {  /*运行时异常*/
            return null;
        }
    }

    /*创建JWT*/
    public String createJwt(UserDetails details,int id,String username){
        Algorithm algorithm=Algorithm.HMAC256(key);
        Date expire=this.expireTime();
        return JWT
                .create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("id", id)
                .withClaim("name", username)
                .withClaim("authorities",
                        details.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
                )   /*用户信息*/
                .withExpiresAt(expire)  /*过期时间*/
                .withIssuedAt(new Date())  /*签发时间*/
                .sign(algorithm);  /*签名*/
    }

    /*获取JWT过期时间*/
    public Date expireTime() {
        Calendar calendar = Calendar.getInstance();  // 获取当前时间
        calendar.add(Calendar.HOUR, expire * 24);  // 增加 expire 天数转换成的小时数
        return calendar.getTime();  // 返回新的时间点作为 Date 对象
    }

    /*去除Token的Bearer*/
    public String convertToken(String headerToken){
        if (headerToken==null|| !headerToken.startsWith("Bearer"))
            return null;
        else
            return headerToken.substring(7);
    }
    /*解析UserDetails存到authorities*/
    public UserDetails toUser(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return User
                .withUsername(claims.get("name").asString())
                .password("******")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }
/*这段代码的主要功能是从一个解码后的 JWT（JSON Web Token）中提取 id 字段，并将其转换为整数类型*/
    public Integer toId(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return claims.get("id").asInt();
    }
}
