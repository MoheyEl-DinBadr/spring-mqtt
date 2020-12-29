package com.mohey.mqtt.core;
/**
 * @author Mohey El-Din Badr
 * @since 2020/12/28
 */
public interface IMQTTSubscriber {

    void subscribeMessage(String topic, int qos);

    void subscribeMessage(String topic);

    void subscribeMessages(String[] topics);

    void subscribeMessages(String[] topics, int[] qos);

    void disconnect();
}
