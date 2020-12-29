package com.mohey.mqtt.core;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface IMQTTPublisher {

    void publishMessage(String topic, String message, int qos, boolean retain);

    void publishMessage(String topic, String message);

    void publishMessage(String topic, MqttMessage message);

    void disconnect();
}
