package com.example.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.example.utils.Const.ORDER_CORS;


/*处理跨域问题，此过滤器应在SpringSecurity的filterChain（优先级为-100）之前
* 要用@Order设置优先级为（-102）*/
@Component
@Order(ORDER_CORS)
public class CorsFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain) throws IOException, ServletException {
        this.addCorsHeader(request, response);

        // 显式处理 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK); // 明确返回 200 状态码
            return; // 直接结束，不进入后续过滤器链
        }
        chain.doFilter(request, response);
    }

    private void addCorsHeader(HttpServletRequest request, HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        response.addHeader("Access-Control-Allow-Credentials", "true"); // 若需要凭证（如 Cookie）
        response.addHeader("Access-Control-Max-Age", "3600"); // 缓存预检结果，减少请求
    }
}
