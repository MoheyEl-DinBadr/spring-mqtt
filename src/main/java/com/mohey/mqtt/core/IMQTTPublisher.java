package com.mohey.mqtt.core;
/**
 * @author Mohey El-Din Badr
 * @since 2020/12/28
 */
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface IMQTTPublisher extends IMQTTClient{

    void publishMessage(String topic, String message, int qos, boolean retain) throws MqttException;

    void publishMessage(String topic, String message) throws MqttException;

    void publishMessage(String topic, MqttMessage message) throws MqttException;
}
