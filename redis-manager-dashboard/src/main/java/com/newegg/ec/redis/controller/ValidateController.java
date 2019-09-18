package com.newegg.ec.redis.controller;

import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.entity.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 通用的校验接口，特殊接口则会放在其他对应的 controller 中
 *
 * @author Jay.H.Zou
 * @date 9/18/2019
 */
@RequestMapping("/validate/*")
@Controller
public class ValidateController {

    public Result validateAddress(String address) {
        return Result.successResult();
    }

}
