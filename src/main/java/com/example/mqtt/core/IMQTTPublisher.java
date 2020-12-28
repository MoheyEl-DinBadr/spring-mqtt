package com.example.mqtt.core;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface IMQTTPublisher {

    public void publishMessage(String topic, String message, int qos, boolean retain);

    public void publishMessage(String topic, String message);

    public void publishMessage(String topic, MqttMessage message);

    public void disconnect();
}
