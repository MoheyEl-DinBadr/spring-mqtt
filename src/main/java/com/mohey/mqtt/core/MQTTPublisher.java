package com.mohey.mqtt.core;

/**
 * @author Mohey El-Din Badr
 * @since 2020/12/29
 */

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class MQTTPublisher extends MQTTConfig implements MqttCallbackExtended, IMQTTPublisher {

    private MqttAsyncClient mqttClient;

    private static MQTTPublisher instance;

    private String clientId;

    private static final Logger logger = LoggerFactory.getLogger(MQTTSubscriber.class);

    private MQTTPublisher() {
        instance = this;
    }

    public static MQTTPublisher getInstance(){
        return instance;
    }

    @Override
    public void publishMessage(String topic, String message, int qos, boolean retain) {
        try {
            this.mqttClient.publish(topic, message.getBytes(), qos, retain);
        } catch (MqttException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void publishMessage(String topic, String message) {
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(this.getQos());
            this.mqttClient.publish(topic, mqttMessage);
        } catch (MqttException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void publishMessage(String topic, MqttMessage message) {
        try {
            this.mqttClient.publish(topic, message);
        } catch (MqttException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() {
        try {
            this.mqttClient.disconnect();
        } catch (MqttException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    protected void config() {
        String serverURL = this.getTCP() + this.getUrl() + ":" + this.getPort();
        if(isHasSSl()){
            serverURL = serverURL.replace(this.getTCP(), this.getSSL());
        }
        this.clientId = this.getClientId() + "_pub";

        MemoryPersistence memoryPersistence = new MemoryPersistence();

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setWill("status/"+ clientId, "disconnected".getBytes(), this.getQos(), true);
        if(!this.getUsername().trim().isEmpty()){
            mqttConnectOptions.setUserName(this.getUsername());
        }
        if(!this.getPassword().trim().isEmpty()){
            mqttConnectOptions.setPassword(this.getPassword().toCharArray());
        }

        try {
            this.mqttClient = new MqttAsyncClient(serverURL, clientId, memoryPersistence);
            this.mqttClient.setCallback(this);
            this.mqttClient.connect(mqttConnectOptions);
            Thread mqttClose = new Thread(() -> {
                try {
                    this.mqttClient.close();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            });
            Runtime.getRuntime().addShutdownHook(mqttClose);

        } catch (MqttException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * This method is called when the connection to the server is lost.
     *
     * @param cause the reason behind the loss of connection.
     */
    @Override
    public void connectionLost(Throwable cause) {
        logger.info(cause.getMessage());
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
    public void messageArrived(String topic, MqttMessage message) throws Exception {

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
        logger.info("Delivery Completed");
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
            this.mqttClient.publish("status/" + this.clientId, "connected".getBytes(), this.getQos(), true );
        } catch (MqttException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
