package com.mohey.mqtt.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.mqttv5.client.IMqttMessageListener;
import org.eclipse.paho.mqttv5.common.MqttSubscription;

/**
 * @author Mohey El-Din Badr
 * @date 2021/1/1
 */
@Getter
public class SubscribedTuple extends MqttSubscription {

    private IMqttMessageListener messageListener;

    /**
     * Constructs a subscription with the specified topic with
     * all other values set to defaults.
     * <p>
     * The defaults are:
     * <ul>
     * 	<li>Subscription QoS is set to 1</li>
     * 	<li>Messages published to this topic by the same client will also be received.</li>
     * 	<li>Messages received by this subscription will keep the retain flag (if it is set).</li>
     * 	<li>Retained messages on this topic will be delivered once the subscription has been made.</li>
     * </ul>
     *
     * @param topic The Topic
     */
    private SubscribedTuple(String topic) {
        super(topic);
    }

    public SubscribedTuple(String topic, int qos) {
        super(topic, qos);
    }

    public SubscribedTuple(String topic, int qos, IMqttMessageListener messageListener){
        super(topic, qos);
        this.messageListener = messageListener;
    }
}
