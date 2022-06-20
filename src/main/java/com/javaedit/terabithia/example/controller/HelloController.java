package com.javaedit.terabithia.example.controller;

import com.javaedit.terabithia.annotation.Controller;
import com.javaedit.terabithia.annotation.RequestMapping;
import com.javaedit.terabithia.annotation.ResponseBody;
import com.javaedit.terabithia.annotation.RestController;
import com.javaedit.terabithia.handler.netty.ParamWrapperRequest;
import com.javaedit.terabithia.method.annotation.RequestMethod;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wjw
 * @description: 测试controller
 * @title: HelloController
 * @date 2022/6/11 11:01
 */
@RestController
@RequestMapping(value = "/hello")
public class HelloController {

    /**
     * request url:/hello/testGet?strParam=test&intParam=1&floatParam=2&doubleParam=3&dateParam=2021-01-01
     */
    @RequestMapping(value = "/testGet", method = {RequestMethod.GET})
    public Object testGet(ParamWrapperRequest request, String strParam, Integer intParam, Float floatParam, Double doubleParam) throws Exception{
        System.out.println(request.getParameterNames());
        // 获取参数
        System.out.println("strParam:" + strParam);
        System.out.println("intParam:" + intParam);
        System.out.println("floatParam:" + floatParam);
        System.out.println("doubleParam:" + doubleParam);

        System.out.println("testGet");
        return request.getParameterMap();
    }

    @RequestMapping(value = "/testPost", method = {RequestMethod.POST})
    public String testPost(ParamWrapperRequest request) throws Exception {

        System.out.println("testPost");
        return "123";
    }

    @ResponseBody
    @RequestMapping(value = "/testJson")
    public Object testJson(FullHttpRequest request) {
        System.out.println("testJson");
        Map<String, Object> map = new HashMap<>();
        return map;
    }
}
