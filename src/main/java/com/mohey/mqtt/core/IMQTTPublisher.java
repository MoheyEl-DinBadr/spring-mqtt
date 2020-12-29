package com.mohey.mqtt.core;
/**
 * @author Mohey El-Din Badr
 * @since 2020/12/28
 */
import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface IMQTTPublisher {

    void publishMessage(String topic, String message, int qos, boolean retain);

    void publishMessage(String topic, String message);

    void publishMessage(String topic, MqttMessage message);

    void disconnect();
}
