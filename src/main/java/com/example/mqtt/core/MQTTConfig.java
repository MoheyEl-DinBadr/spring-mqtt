package com.example.mqtt.core;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

@Getter
public abstract class MQTTConfig {

    @Value("mqtt.brokerURL")
    private String url;

    @Value("mqtt.port")
    private int port;

    @Value("mqtt.username")
    private String username;

    @Value("mqtt.password")
    private String password;

    @Value("mqtt.hasSSL")
    private boolean hasSSl;

    @Value("mqtt.clientId")
    private String clientId;

    private String TCP = "tcp://";

    private  String SSL = "ssl://";

    protected abstract void config();

}
