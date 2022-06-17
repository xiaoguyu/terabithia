package com.javaedit.terabithia.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author wjw
 * @description: 配置
 * @title: TerabithiaProperties
 * @date 2022/6/10 17:49
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "terabithia")
public class TerabithiaProperties {

    /**
     * Server HTTP port.
     */
    private Integer port;

}
