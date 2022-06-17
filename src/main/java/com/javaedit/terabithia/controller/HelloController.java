package com.javaedit.terabithia.controller;

import com.javaedit.terabithia.annotation.Controller;
import com.javaedit.terabithia.annotation.RequestMapping;
import com.javaedit.terabithia.method.annotation.RequestMethod;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wjw
 * @description: 测试controller
 * @title: HelloController
 * @date 2022/6/11 11:01
 */
@Controller
@RequestMapping(value = "/hello", method = {RequestMethod.GET, RequestMethod.POST})
public class HelloController {

    @RequestMapping(value = "/index", method = {RequestMethod.POST})
    public Object index(FullHttpRequest request) {
        System.out.println("hello world");
        Map<String, String> map = new HashMap<>();
        map.put("name", "wjw");
        return map;
    }
}
