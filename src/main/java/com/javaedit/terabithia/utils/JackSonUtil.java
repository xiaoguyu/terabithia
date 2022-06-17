package com.javaedit.terabithia.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @description: jackson工具类
 * @title: JackSonUtil
 * @date 2022/6/15 11:16
 */
public class JackSonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(JackSonUtil.class);

    static {
        JavaTimeModule timeModule = new JavaTimeModule();
        // 设置LocalDateTime的序列化格式
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        MAPPER.registerModule(timeModule);
    }

    private JackSonUtil() {
    }

    public static String toJsonString(Object object) {
        if (object == null) {
            throw new RuntimeException("object is null, unable to convert");
        }

        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON transformation error");
        }
    }

    public static <T> T toObject(String json, Class<T> cla, String exceptionContent) {
        checkJsonString(json);

        try {
            return MAPPER.readValue(json, cla);
        } catch (IOException e) {
            if ("".equals(exceptionContent) || exceptionContent == null) {
                throw new RuntimeException("json string cannot be converted to object");
            }
            throw new RuntimeException(exceptionContent);
        }
    }

    private static void checkJsonString(String json) {
        if (json == null) {
            LOG.warn("json is null");
        }
        if ("".equals(json)) {
            LOG.warn("json is empty");
        }
    }
}
