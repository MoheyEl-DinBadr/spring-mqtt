package com.mohey.mqtt.core;
/**
 * @author Mohey El-Din Badr
 * @since 2020/12/28
 */
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;

@Getter
public abstract class MQTTConfig {

    @Value("${mqtt.brokerURL:127.0.0.1}")
    private String url;

    @Value("${mqtt.port:1883}")
    private int port;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Value("${mqtt.hasSSL:false}")
    private boolean hasSSl;

    @Value("${mqtt.clientId:}")
    private String clientId;

    private String TCP = "tcp://";

    private  String SSL = "ssl://";

    @PostConstruct
    protected abstract void config();

}
