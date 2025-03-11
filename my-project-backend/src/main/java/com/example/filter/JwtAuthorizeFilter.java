package com.example.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.utils.Const;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthorizeFilter extends OncePerRequestFilter {

    @Resource
    JwtUtils utils;

/*
* 写自定义过滤规则 */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorization=request.getHeader("Authorization"); /*获取JWT*/
        DecodedJWT jwt=utils.resolveJWT(authorization);  /*解析JWT*/
        /*解析UserDetails存到authorities*/
        if (jwt!=null){
            /*使用 utils.toUser(jwt) 方法从 JWT 中提取用户信息，并返回一个 UserDetails 对象。*/
            UserDetails user=utils.toUser(jwt);
            /*这是Security用来封装用户身份和权限信息的类，构造函数中的参数依次为：用户身份信息、凭证、用户的权限集合*/
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            /*这里authentication又被加入了WebAuthenticationDetailsSource获得的更多认证细节*/
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            /*将用户的认证信息存储到 SecurityContextHolder 的全局的上下文管理器中，
            从而允许在应用的任何地方访问和操作当前用户的身份认证信息*/
            SecurityContextHolder.getContext().setAuthentication(authentication);
            /*将用户 ID 设置到请求属性中*/
            request.setAttribute(Const.ATTR_USER_ID, utils.toId(jwt));
        }
        filterChain.doFilter(request,response);

    }

}
