package com.mohey.mqtt.core;

public interface IMQTTSubscriber {

    void subscribeMessage(String topic, int qos);

    void subscribeMessage(String topic);

    void subscribeMessages(String[] topics);

    void subscribeMessages(String[] topics, int[] qos);

    void disconnect();
}
