package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.vo.request.ConfirmResetVO;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.entity.vo.request.EmailResetVO;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.ControllerUtils;
import com.example.utils.IpUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Function;
import java.util.function.Supplier;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {
    @Resource
    AccountService service;




 @GetMapping("/ask-code")
 public RestBean<Void> askVerifyCode(
         @RequestParam @Email String email,
         @RequestParam @Pattern(regexp = "(register|reset|modify)") String type,
         HttpServletRequest request) {
     String rawIp = request.getRemoteAddr();
     String normalizedIp = IpUtils.normalizeIp(rawIp); // 标准化IP
     System.out.println("限流检查IP: " + normalizedIp + ", Key: " + Const.VERIFY_EMAIL_LIMIT + normalizedIp);
     return this.messageHandle(() -> service.registerEmailVerifyCode(type, email, normalizedIp));
 }


/*方法返回类型是RestBean<Void>，这可能是一个自定义的结果包装类，用来封装API响应的状态、消息等信息。
参数@RequestBody @Valid EmailRegisterVO vo表示从HTTP请求体中接收的数据将会被映射到EmailRegisterVO对象中。
@Valid注解用于触发对该对象的验证，确保传入的数据符合预定义的约束条件（例如，字段是否为空、格式是否正确等）*/
    @PostMapping("/register")
    public RestBean<Void> register(@RequestBody @Valid EmailRegisterVO vo) {
        return this.messageHandle(() -> service.registerEmailAccount(vo));
    }



    /*重置密码之验证码确认*/
    @PostMapping("/reset-confirm")
    public RestBean<Void> resetConfirm(@RequestBody @Valid ConfirmResetVO vo) {
        return this.messageHandle(vo, service::resetConfirm);
    }

    /*重置密码之修改密码*/
    @PostMapping("/reset-password")
    public RestBean<Void> resetConfirm(@RequestBody @Valid EmailResetVO vo) {
        return this.messageHandle(vo, service::resetEmailAccountPassword);
    }

    public  <T> RestBean<T> messageHandle(Supplier<String> action) {
        String message = action.get();
        if (message == null) {
            return RestBean.success();
        } else {
            return RestBean.failure( 400, message);
        }
    }

    /*将上面的messageHandle方法继续精简*/
    private <T> RestBean<Void> messageHandle(T vo, Function<T, String> function) {
        return messageHandle(() -> function.apply(vo));
    }
}
