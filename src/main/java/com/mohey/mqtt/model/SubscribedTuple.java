package com.mohey.mqtt.model;

import lombok.Getter;
import lombok.NonNull;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;

/**
 * @author Mohey El-Din Badr
 * @date 2021/1/1
 */
@Getter
public class SubscribedTuple {

    @NonNull
    private String topic;

    @NonNull
    private int qos;

    private IMqttMessageListener messageListener;

    public SubscribedTuple(@NonNull String topic, @NonNull int qos, IMqttMessageListener messageListener) {
        this.topic = topic;
        this.qos = qos;
        this.messageListener = messageListener;
    }

    public SubscribedTuple(@NonNull String topic, @NonNull int qos) {
        this.topic = topic;
        this.qos = qos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubscribedTuple tuple = (SubscribedTuple) o;

        if (qos != tuple.qos) return false;
        if (!topic.equals(tuple.topic)) return false;
        return messageListener != null ? messageListener.equals(tuple.messageListener) : tuple.messageListener == null;
    }

    @Override
    public int hashCode() {
        int result = topic.hashCode();
        result = 31 * result + qos;
        result = 31 * result + (messageListener != null ? messageListener.hashCode() : 0);
        return result;
    }
}
