package com.newegg.ec.cache.app.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Jay.H.Zou
 * @date 2018/4/28
 */

@Controller
public class IndexConttroller {

    @RequestMapping("/")
    public String accessIndex(){
        return "index";
    }

    /**
     * TODO: 后期会移动
     */
    @RequestMapping("/pages/createCluster")
    public String accessCreateCluster(){
        return "createCluster";
    }

    /**
     * TODO: 后期会移动
      */
    @RequestMapping("/pages/managerCluster")
    public String accessManagerCluster(){
        return "managerCluster";
    }

}
