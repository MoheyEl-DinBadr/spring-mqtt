package com.mohey.mqtt.model;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;

/**
 * @author MoheyEl-DinBadr@outlook.com
 * @date
 */
@Getter
public class SubscribedTuple {

    private String topic;

    private int qos;

    private IMqttMessageListener messageListener;

    public SubscribedTuple(String topic, int qos, IMqttMessageListener messageListener) {
        this.topic = topic;
        this.qos = qos;
        this.messageListener = messageListener;
    }
}
