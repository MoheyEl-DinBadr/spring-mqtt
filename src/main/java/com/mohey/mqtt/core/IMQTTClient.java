package com.mohey.mqtt.core;

import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * @author MoheyEl-DinBadr@outlook.com
 * @date 2021/1/7
 */
public interface IMQTTClient {
    boolean isConnected();

    void disconnect() throws MqttException;
}
