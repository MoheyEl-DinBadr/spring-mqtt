package com.mohey.mqtt.core;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * @author Mohey El-Din Badr
 * @since 2020/12/28
 */
public interface IMQTTSubscriber extends IMQTTClient{

    void subscribeMessage(String topic, int qos) throws MqttException;

    void subscribeMessage(String topic) throws MqttException;

    void subscribeMessage(String topic, IMqttMessageListener messageListener) throws MqttException;

    void subscribeMessage(String topic, int qos, IMqttMessageListener messageListener) throws MqttException;

    void subscribeMessages(String[] topics) throws MqttException;

    void subscribeMessages(String[] topics, int[] qos) throws MqttException;

    void subscribeMessages(String[] topics, IMqttMessageListener[] messageListeners) throws MqttException;

    void subscribeMessages(String[] topics, int[] qos, IMqttMessageListener[] messageListeners) throws MqttException;

    void unsubscribeMessage(String topic) throws MqttException;

    void unsubscribeMessages(String[] topics) throws MqttException;
}
