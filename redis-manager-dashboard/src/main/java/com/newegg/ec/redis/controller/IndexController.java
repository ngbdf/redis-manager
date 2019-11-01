package com.newegg.ec.redis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Jay.H.Zou
 * @date 2019/7/8
 */
@Controller
public class IndexController {

    @RequestMapping({"/", "index", "templates/index.html"})
    public String index() {
        return "index";
    }
}
