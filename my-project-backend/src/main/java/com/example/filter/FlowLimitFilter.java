package com.example.filter;

import com.example.entity.RestBean;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Order(Const.ORDER_LIMIT)
public class FlowLimitFilter extends HttpFilter {

    @Resource
    StringRedisTemplate template;

    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain) throws IOException, ServletException {
        // 跳过 OPTIONS 预检请求的计数
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String address = request.getRemoteAddr();
        if (this.tryCount(address)) {
            chain.doFilter(request, response);
        } else {
            this.writeBlockMessage(response);
        }
    }

    private void writeBlockMessage(HttpServletResponse response) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(RestBean.forbidden("操作频繁，请稍后再试").asJsonString());
    }


    private boolean tryCount(String ip) {
        synchronized (ip.intern()) {
            if (Boolean.TRUE.equals(template.hasKey(Const.FLOW_LIMIT_BLOCK + ip))) {
                return false;
            }
            return this.limitPeriodCheck(ip);
        }
    }

    private boolean limitPeriodCheck(String ip) {
        String counterKey = Const.FLOW_LIMIT_COUNTER + ip;
        // 使用 Redis 的 INCR 和 EXPIRE 组合操作（确保原子性）
        Long count = template.opsForValue().increment(counterKey);
        if (count == 1) {
            // 首次设置过期时间
            template.expire(counterKey, 3, TimeUnit.SECONDS);
        }
        if (count != null && count > 100) {
            template.opsForValue().set(Const.FLOW_LIMIT_BLOCK + ip, "", 30, TimeUnit.SECONDS);
            return false;
        }
        return true;
    }

}
