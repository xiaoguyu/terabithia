package com.javaedit.terabithia.example.controller;

import com.javaedit.terabithia.annotation.Controller;
import com.javaedit.terabithia.annotation.RequestMapping;
import com.javaedit.terabithia.annotation.ResponseBody;
import com.javaedit.terabithia.method.annotation.RequestMethod;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wjw
 * @description: 测试controller
 * @title: HelloController
 * @date 2022/6/11 11:01
 */
@Controller
@RequestMapping(value = "/hello")
public class HelloController {

    @RequestMapping(value = "/testGet", method = {RequestMethod.GET})
    public String testGet(FullHttpRequest request) throws Exception{
        // 获取参数
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> params = queryStringDecoder.parameters();
        System.out.println(params);

        System.out.println("testGet");
        return "123";
    }

    @RequestMapping(value = "/testPost", method = {RequestMethod.POST})
    public String testPost(FullHttpRequest request) throws Exception {
        // 输出参数
        HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(factory, request);
        List<InterfaceHttpData> datas = decoder.getBodyHttpDatas();
        for (InterfaceHttpData data : datas) {
            System.out.println(data.getHttpDataType());
            Attribute attribute = (Attribute) data;
            System.out.println(attribute.getName());
            System.out.println(attribute.getValue());
        }

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
