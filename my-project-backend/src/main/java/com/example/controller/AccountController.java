package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.dto.AccountDetails;
import com.example.entity.vo.request.ChangePasswordVO;
import com.example.entity.vo.request.DetailsSaveVO;
import com.example.entity.vo.request.ModifyEmailVO;
import com.example.entity.vo.request.PrivacySaveVO;
import com.example.entity.vo.response.AccountDetailsVO;
import com.example.entity.vo.response.AccountPrivacyVO;
import com.example.entity.vo.response.AccountVO;
import com.example.service.AccountDetailsService;
import com.example.service.AccountPrivacyService;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.ControllerUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/user")
public class AccountController {

    @Resource
    AccountService accountService;

    @Resource
    AccountDetailsService accountDetailsService;

    @Resource
    AccountPrivacyService privacyService;

    @Resource
    ControllerUtils utils;

    @GetMapping("/info")
    public RestBean<AccountVO> info(@RequestAttribute(Const.ATTR_USER_ID) int id){

        Account account=accountService.findAccountById(id);
        return RestBean.success(account.asViewObject(AccountVO.class));
    }


    @GetMapping(value = "/details")
    public RestBean<AccountDetailsVO> details(@RequestAttribute(Const.ATTR_USER_ID) int id) {
        AccountDetails details = Optional.ofNullable(accountDetailsService.findAccountDetailsById(id))
                .orElseGet(AccountDetails::new);
        return RestBean.success(details.asViewObject(AccountDetailsVO.class));
    }

    @PostMapping(value = "/save-details")
    public RestBean<Void> saveDetails(@RequestAttribute(Const.ATTR_USER_ID) int id,
                                      @RequestBody @Valid DetailsSaveVO vo) {
        boolean success = accountDetailsService.saveAccountDetails(id, vo);
        return success ? RestBean.success() : RestBean.failure(400, "此用户名已被其他用户使用，请重新更换！");
    }

    @PostMapping(value = "/modify-email")
    public RestBean<Void> modifyEmail(@RequestAttribute(Const.ATTR_USER_ID) int id,
                                      @RequestBody @Valid ModifyEmailVO vo) {
        String result = accountService.modifyEmail(id, vo);
        return result == null ? RestBean.success() : RestBean.failure(400, result);
    }

    @PostMapping(value = "/change-password")
    public RestBean<Void> changePassword(@RequestAttribute(Const.ATTR_USER_ID) int id,
                                         @RequestBody @Valid ChangePasswordVO vo) {
        return utils.messageHandle(() -> accountService.changePassword(id, vo));
    }




    @PostMapping(value = "/save-privacy")
    public RestBean<Void> savePrivacy(@RequestAttribute(Const.ATTR_USER_ID) int id,
                                      @RequestBody @Valid PrivacySaveVO vo) {
        privacyService.savePrivacy(id, vo);
        return RestBean.success();
    }

    @GetMapping(value = "/privacy")
    public RestBean<AccountPrivacyVO> privacy(@RequestAttribute(Const.ATTR_USER_ID) int id) {
        return RestBean.success(privacyService.accountPrivacy(id).asViewObject(AccountPrivacyVO.class));
    }
}
