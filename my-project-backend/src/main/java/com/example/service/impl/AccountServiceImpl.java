package com.example.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Account;
import com.example.entity.dto.AccountDetails;
import com.example.entity.dto.AccountPrivacy;
import com.example.entity.vo.request.*;
import com.example.mapper.AccountDetailsMapper;
import com.example.mapper.AccountMapper;
import com.example.mapper.AccountPrivacyMapper;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Resource
    AmqpTemplate amqpTemplate;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    FlowUtils utils;

    @Resource
    PasswordEncoder encoder;

    @Resource
    AccountDetailsMapper detailsMapper;

    @Resource
    AccountPrivacyMapper privacyMapper;


    /*查询自定义的用户信息
    * 实现UserDetailsService接口的方法，用于根据提供的用户名（可能为用户名或邮箱）加载用户特定数据。*/
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account=this.findUserByNameOrEmail(username);
        if (account == null)
            throw new UsernameNotFoundException("用户名或密码错误");
        return User
                /*username可能是邮箱或用户名,而account.getUsername()只能是用户名*/
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    /*text为用户名或邮箱
    * 用户查询*/
    public Account findUserByNameOrEmail(String text){
        return this.query()
                .eq("username",text).or()
                .eq("email",text)
                .one();
    }

    @Override
    public String registerEmailVerifyCode(String type, String email, String ip) {
        synchronized (ip.intern()) {
            if (!this.verifyLimit(ip)) {
                return "请求频繁，请稍后再试";
            }
            Random random = new Random();
            int code = random.nextInt(899999) + 100000;
            Map<String, Object> data = Map.of("type", type, "email", email, "code", code);
            amqpTemplate.convertAndSend("mail", data);
            stringRedisTemplate.opsForValue()
                    .set(Const.VERIFY_EMAIL_DATA + email, String.valueOf(code), 3, TimeUnit.MINUTES);
            return null;
        }
    }

    private boolean verifyLimit(String ip) {
        String key = Const.VERIFY_EMAIL_LIMIT + ip;
        return utils.limitOnceCheck(key, 60);
    }

    public String registerEmailAccount(EmailRegisterVO vo) {
        String email = vo.getEmail();
        String username=vo.getUsername();
        String key=Const.VERIFY_EMAIL_DATA + email;
        String code = stringRedisTemplate.opsForValue().get(key);
        if (code == null) {
            return "请先获取验证码";
        }
        if (!code.equals(vo.getCode())) {
            return "验证码输入错误，请重新输入";
        }
        if (this.existsAccountByEmail(email)) {
            return "此电子邮件已被其他用户注册";
        }
        if (this.existsAccountByUsername(username)) {
            return "此用户名已被其他人注册，请更新一个新的用户名";
        }
        String password = encoder.encode(vo.getPassword());
        Account account = new Account(null, username, password, email, "user", null,new Date());
        if (this.save(account)) {
            /*这注册成功要删除redis里的验证码*/
            stringRedisTemplate.delete(key);
            privacyMapper.insert(new AccountPrivacy(account.getId()));
            AccountDetails details = new AccountDetails();
            details.setId(account.getId());
            detailsMapper.insert(details);
            return null;
        } else {
            return "内部错误，请联系管理员";
        }
    }

    private boolean existsAccountByEmail(String email) {
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email", email));
    }

    private boolean existsAccountByUsername(String username) {
        return this.baseMapper.exists(Wrappers.<Account>query().eq("username", username));
    }


    @Override
    public String resetConfirm(ConfirmResetVO vo) {
        String email = vo.getEmail();
        String code = stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA + email);
        if (code == null) {
            return "请先获取验证码";
        }
        if (!code.equals(vo.getCode())) {
            return "验证码错误，请重新输入";
        }
        return null;
    }

    @Override
    public String resetEmailAccountPassword(EmailResetVO vo) {
        String email = vo.getEmail();
        /*此处调用resetConfirm的参数必须将EmailResetVO的vo转变（用实体类的全参构造方法）*/
        String verify = this.resetConfirm(new ConfirmResetVO(email, vo.getCode()));
        if (verify != null) {
            return verify;
        }
        String password = encoder.encode(vo.getPassword());
        boolean update = this.update().eq("email", email).set("password", password).update();
        if (update) {
            stringRedisTemplate.delete(Const.VERIFY_EMAIL_DATA + email);
        }
        return null;
    }


    @Override
    public Account findAccountById(int id) {
        return this.query().eq("id",id).one();
    }

    @Override
    public String modifyEmail(int id, ModifyEmailVO vo) {
        String email = vo.getEmail();
        String code = getEmailVerifyCode(email);
        if (code == null) return "请先获取验证码！";
        if (!code.equals(vo.getCode())) return "验证码错误，请重新输入";
        this.deleteEmailVerifyCode(email);
        Account account = this.findAccountByNameOrEmail(email);
        if (account != null && account.getId() != id)
            return "该电子邮件已经被其他账号绑定，无法完成此操作！";
        this.update()
                .set("email", email)
                .eq("id", id)
                .update();
        return null;
    }

    @Override
    public String changePassword(int id, ChangePasswordVO vo) {
        String password = this.query()
                .eq("id", id)
                .one()
                .getPassword();
        if (!encoder.matches(vo.getPassword(), password)) {
            return "原密码错误，请重新输入！";
        }
        boolean success = this.update()
                .eq("id", id)
                .set("password", encoder.encode(vo.getNew_password()))
                .update();
        return success ? null : "未知错误，请联系管理员";
    }

    /**
     * 获取Redis中存储的邮件验证码
     * @param email 电邮
     * @return 验证码
     */
    private String getEmailVerifyCode(String email){
        String key = Const.VERIFY_EMAIL_DATA + email;
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 通过用户名或邮件地址查找用户
     * @param text 用户名或邮件
     * @return 账户实体
     */
    public Account findAccountByNameOrEmail(String text){
        return this.query()
                .eq("username", text).or()
                .eq("email", text)
                .one();
    }

    /**
     * 移除Redis中存储的邮件验证码
     * @param email 电邮
     */
    private void deleteEmailVerifyCode(String email){
        String key = Const.VERIFY_EMAIL_DATA + email;
        stringRedisTemplate.delete(key);
    }
}
