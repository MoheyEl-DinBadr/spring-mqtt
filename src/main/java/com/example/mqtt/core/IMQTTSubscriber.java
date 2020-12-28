package com.example.mqtt.core;

public interface IMQTTSubscriber {

    public void subscribeMessage(String topic, int qos);

    public void subscribeMessage(String topic);

    public void subscribeMessages(String[] topics);

    public void subscribeMessages(String[] topics, int[] qos);

    public void disconnect();
}
