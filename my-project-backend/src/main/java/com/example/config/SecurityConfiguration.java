package com.example.config;

import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.vo.response.AuthorizeVO;
import com.example.filter.JwtAuthorizeFilter;
import com.example.service.AccountService;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

@Configuration
public class SecurityConfiguration {
    @Resource
    JwtUtils utils;
    @Resource
    JwtAuthorizeFilter jwtAuthorizeFilter;

    @Resource
    AccountService service;

    /*@Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173")); // 设置允许的源
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 设置允许的方法
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type")); // 设置允许的头部
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 对所有路径应用此CORS配置
        return source;
    }*/

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        return http
                .authorizeHttpRequests(conf->conf
                        .requestMatchers("/api/auth/**","/error").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(conf->conf
                        .loginProcessingUrl("/api/auth/login")
                        .failureHandler(this::onAuthenticationFailure)
                        .successHandler(this::onAuthenticationSuccess)
                )
                .logout(conf->conf
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(this::onLogoutSuccess)
                )
                .exceptionHandling(conf->conf
                        .authenticationEntryPoint(this::Unauthorized)/*未登录*/
                        .accessDeniedHandler(this::onAccessDeny) /*登录但是权限不足*/
                )
                .csrf(AbstractHttpConfigurer::disable)/*关csrf*/
                .sessionManagement(conf->conf
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )/*无状态化session*/
                .addFilterBefore(jwtAuthorizeFilter, UsernamePasswordAuthenticationFilter.class)
                //.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .build();
    }

    /*登录但是权限不足*/
    public void onAccessDeny(HttpServletRequest request,
                             HttpServletResponse response,
                             AccessDeniedException exception) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(RestBean.forbidden(exception.getMessage()).asJsonString());
    }

    /*未登录处理*/
    public void Unauthorized(HttpServletRequest request,
                             HttpServletResponse response,
                             AuthenticationException exception) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(RestBean.unauthorized(exception.getMessage()).asJsonString());
    }
    /*登录失败处理*/
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(RestBean.unauthorized(exception.getMessage()).asJsonString());/*exception.getMessage()标准*/
    }

/*登录成功处理*/
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        User user =(User) authentication.getPrincipal();  /*SpringSecurity的User*/
        Account account=service.findUserByNameOrEmail(user.getUsername());

        String token=utils.createJwt(user,account.getId(), account.getUsername());
        AuthorizeVO vo=new AuthorizeVO();

        /*将account（DTO）里存的数据复制到vo(VO)中*/
        BeanUtils.copyProperties(account,vo);
        vo.setExpire(utils.expireTime());
        vo.setToken(token);

        response.getWriter().write(RestBean.success(vo).asJsonString());
    }
/*退出登录处理*/
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter writer = response.getWriter();
        String authorization=request.getHeader("Authorization");

        if (utils.invalidateJwt(authorization)){
            writer.write(RestBean.success().asJsonString());
        }else {
            writer.write(RestBean.failure(400,"退出登录失败").asJsonString());
        }
    }
}
