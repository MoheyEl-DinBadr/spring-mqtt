package com.mohey.mqtt.core;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;

/**
 * @author Mohey El-Din Badr
 * @since 2020/12/28
 */
public interface IMQTTSubscriber {

    void subscribeMessage(String topic, int qos);

    void subscribeMessage(String topic);

    void subscribeMessage(String topic, IMqttMessageListener messageListener);

    void subscribeMessage(String topic, int qos, IMqttMessageListener messageListener);

    void subscribeMessages(String[] topics);

    void subscribeMessages(String[] topics, int[] qos);

    void subscribeMessages(String[] topics, IMqttMessageListener[] messageListeners);

    void subscribeMessages(String[] topics, int[] qos, IMqttMessageListener[] messageListeners);

    void disconnect();
}
