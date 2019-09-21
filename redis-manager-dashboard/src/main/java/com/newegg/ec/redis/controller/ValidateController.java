package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.entity.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 通用的校验接口，特殊接口则会放在其他对应的 controller 中
 *
 * @author Jay.H.Zou
 * @date 9/18/2019
 */
@RequestMapping("/validate/*")
@Controller
public class ValidateController {

    @RequestMapping(value = "/address/{address}", method = RequestMethod.GET)
    @ResponseBody
    public Result validateAddress(@PathVariable("address") String address) {
        return Result.failResult();
    }

}
