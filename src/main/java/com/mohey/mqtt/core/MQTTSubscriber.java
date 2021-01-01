package com.mohey.mqtt.core;
/**
 * @author Mohey El-Din Badr
 * @since 2020/12/28
 */

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MQTTSubscriber extends MQTTConfig implements MqttCallbackExtended, IMQTTSubscriber{

    private static final Logger logger = LoggerFactory.getLogger(MQTTSubscriber.class);
    private static MQTTSubscriber instance;
    private MqttClient mqttClient;
    private Map<String, Integer> topics;
    private String clientId;
    private MqttConnectOptions mqttConnectOptions;
    private MemoryPersistence memoryPersistence;

    private MQTTSubscriber(){
        instance = this;
    }

    public static MQTTSubscriber getInstance(){
        return instance;
    }

    @Override
    public void subscribeMessage(String topic, int qos) {
        try {
            mqttClient.subscribe(topic,qos);
            this.topics.put(topic, qos);
        } catch (MqttException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void subscribeMessage(String topic) {
        try {
            mqttClient.subscribe(topic);
            this.topics.put(topic, 1);
        } catch (MqttException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void subscribeMessages(String[] topics) {
        try {
            mqttClient.subscribe(topics);
            for(String topic : topics){
                this.topics.put(topic, 1);
            }
        } catch (MqttException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void subscribeMessages(String[] topics, int[] qos) {
        try {
            mqttClient.subscribe(topics, qos);
            for(int i=0; i < topics.length; i++){
                this.topics.put(topics[i], qos[i]);
            }
        } catch (MqttException e) {
            logger.error(e.getMessage(), e);
        }

    }

    @Override
    public void disconnect() {
        try {
            mqttClient.disconnect();
        } catch (MqttException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Called when the connection to the server is completed successfully.
     *
     * @param reconnect If true, the connection was the result of automatic reconnect.
     * @param serverURI The server URI that the connection was made to.
     */

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {

        try {
            this.mqttClient.publish("status/" + this.clientId,"alive".getBytes(), 2, true );
        } catch (MqttException e) {
            logger.error(e.getMessage(), e);
        }
        if(topics != null && !topics.isEmpty()){
            for(String topic : topics.keySet()){
                try {
                    mqttClient.subscribe(topic, topics.get(topic));
                } catch (MqttException e) {
                    logger.error(e.getMessage() + ", " + topic, e);
                }
            }
        }
    }

    /**
     * This method is called when the connection to the server is lost.
     *
     * @param cause the reason behind the loss of connection.
     */
    @Override
    public void connectionLost(Throwable cause) {
        logger.info("Connection Lost");
    }

    /**
     * This method is called when a message arrives from the server.
     *
     * <p>
     * This method is invoked synchronously by the MQTT client. An
     * acknowledgment is not sent back to the server until this
     * method returns cleanly.</p>
     * <p>
     * If an implementation of this method throws an <code>Exception</code>, then the
     * client will be shut down.  When the client is next re-connected, any QoS
     * 1 or 2 messages will be redelivered by the server.</p>
     * <p>
     * Any additional messages which arrive while an
     * implementation of this method is running, will build up in memory, and
     * will then back up on the network.</p>
     * <p>
     * If an application needs to persist data, then it
     * should ensure the data is persisted prior to returning from this method, as
     * after returning from this method, the message is considered to have been
     * delivered, and will not be reproducible.</p>
     * <p>
     * It is possible to send a new message within an implementation of this callback
     * (for example, a response to this message), but the implementation must not
     * disconnect the client, as it will be impossible to send an acknowledgment for
     * the message being processed, and a deadlock will occur.</p>
     *
     * @param topic   name of the topic on the message was published to
     * @param message the actual message.
     * @throws Exception if a terminal error has occurred, and the client should be
     *                   shut down.
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            logger.info("Topic: " + topic + ", " +
                    "Message: " + message.toString());
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }

    }

    /**
     * Called when delivery for a message has been completed, and all
     * acknowledgments have been received. For QoS 0 messages it is
     * called once the message has been handed to the network for
     * delivery. For QoS 1 it is called when PUBACK is received and
     * for QoS 2 when PUBCOMP is received. The token will be the same
     * token as that returned when the message was published.
     *
     * @param token the delivery token associated with the message.
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    @Override
    protected void config() {
        String serverURL = this.getTCP() + this.getUrl() + ":" + this.getPort();
        if(isHasSSl()){
            serverURL = this.getSSL() + this.getUrl() + ":" + this.getPort();
        }

        this.clientId = this.getClientId() + "_sub";
        this.memoryPersistence = new MemoryPersistence();

        mqttConnectOptions = new MqttConnectOptions();
        this.mqttConnectOptions.setAutomaticReconnect(true);
        this.mqttConnectOptions.setCleanSession(true);
        this.mqttConnectOptions.setWill("status/"+this.clientId, "disconnected".getBytes(), 2, true);
        if(!this.getUsername().trim().isEmpty()){
            this.mqttConnectOptions.setUserName(this.getUsername());
        }
        if(!this.getPassword().trim().isEmpty()){
            this.mqttConnectOptions.setPassword(this.getPassword().toCharArray());
        }

        try {
            this.mqttClient = new MqttClient(serverURL, this.clientId, this.memoryPersistence);
            this.mqttClient.setCallback(this);
            this.mqttClient.connect(this.mqttConnectOptions);
            this.topics = new HashMap<>();
            Thread mqttClose = new Thread(() -> {
                try {
                    this.mqttClient.close();
                } catch (MqttException e) {
                    logger.error(e.getMessage(), e);
                }
            });
            Runtime.getRuntime().addShutdownHook(mqttClose);

        } catch (MqttException e) {
            logger.error(e.getMessage(), e);
        }

    }
}
