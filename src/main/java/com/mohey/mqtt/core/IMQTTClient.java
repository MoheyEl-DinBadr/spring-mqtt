package com.mohey.mqtt.core;

import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * @author MoheyEl-DinBadr@outlook.com
 * @date
 */
public interface IMQTTClient {
    boolean isConnected();

    void disconnect() throws MqttException;
}
