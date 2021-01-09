package com.mohey.mqtt.core;

import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;

/**
 * @author Mohey El-Din Badr
 * @since 2020/12/28
 */

public interface IMQTTPublisher extends IMQTTClient{

    void publishMessage(String topic, String message, int qos, boolean retain) throws MqttException;

    void publishMessage(String topic, String message) throws MqttException;

    void publishMessage(String topic, MqttMessage message) throws MqttException;
}
