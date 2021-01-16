package com.mohey.mqtt.core;
/**
 * @author Mohey El-Din Badr
 * @since 2020/12/28
 */

import com.mohey.mqtt.model.SubscribedTuple;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class MQTTSubscriber extends MQTTConfig implements MqttCallbackExtended, IMQTTSubscriber{
    
    private static MQTTSubscriber instance;
    @Getter
    private MqttClient mqttClient;

    private String clientId;

    @Getter
    private final Map<String, SubscribedTuple> subscribedTuples = new HashMap<>();

    private MQTTSubscriber(){
        instance = this;
    }

    /**
     * If used must be sure that the bean has been created
     * @return returns instance of the MQTTSubscriber
     */
    public static MQTTSubscriber getInstance(){
        return instance;
    }

    /**
     * Subscribe to desired topic with default qos
     * @param topic String topic to subscribe to
     * @throws MqttException if there was an error registering the subscription.
     */
    @Override
    public void subscribeMessage(String topic) throws MqttException {
        this.subscribeMessage(topic, this.getQos());
    }

    /**
     * Subscribe to a topic provided qos
     * @param topic String topic to subscribe to
     * @param qos int qos to subscribe ex.(qos=0,1,2)
     * @throws MqttException if there was an error registering the subscription.
     */
    @Override
    public void subscribeMessage(String topic, int qos) throws MqttException{
        this.mqttClient.subscribe(topic,qos);
        log.info("Subscribed to Topic: " + topic + ", Qos: " + this.getQos());
        this.subscribedTuples.put(topic, new SubscribedTuple(topic, qos));
    }

    /**
     * Subscribe to desired topic with default qos while providing messageListener to handle received message
     * @param topic String topic to subscribe to
     * @param messageListener a callback to handle incoming messages
     * @throws MqttException if there was an error registering the subscription.
     */
    @Override
    public void subscribeMessage(String topic, IMqttMessageListener messageListener) throws MqttException{
        this.subscribeMessage(topic, this.getQos(), messageListener);
    }

    /**
     *
     * @param topic String topic to subscribe to
     * @param qos int qos to subscribe ex.(qos=0,1,2)
     * @param messageListener a callback to handle incoming messages
     * @throws MqttException if there was an error registering the subscription.
     */
    @Override
    public void subscribeMessage(String topic, int qos, IMqttMessageListener messageListener) throws MqttException{
        this.mqttClient.subscribe(topic, qos, messageListener);
        log.info("Subscribed to Topic: " + topic + ", Qos: " + this.getQos() + ", MessageListener: " + messageListener.getClass().getName());
        this.subscribedTuples.put(topic, new SubscribedTuple(topic, qos, messageListener));
    }

    /**
     * Subscribe to an array of topics with the default qos
     * @param topics String array of topics to subscribe to
     * @throws MqttException if there was an error registering the subscription.
     */
    @Override
    public void subscribeMessages(String[] topics) throws MqttException{
        int[] qos = new int[topics.length];
        Arrays.fill(qos, this.getQos());
        this.subscribeMessages(topics, qos);
    }

    /**
     * Subscribe to a group of topics providing a qos for each topic
     * @param topics String array of topics to subscribe to
     * @param qos int array of qos values (0,1,2)
     * @throws MqttException if there was an error registering the subscription.
     */
    @Override
    public void subscribeMessages(String[] topics, int[] qos) throws MqttException{
        this.mqttClient.subscribe(topics, qos);
        log.info("Subscribed to mqtt topics");
        for(int i=0; i< topics.length; i++){
            this.subscribedTuples.put(topics[i], new SubscribedTuple(topics[i], qos[i]));
        }
    }

    /**
     * Subscribe to a group of topics providing message handler for each topic with default qos
     * @param topics String array of topics
     * @param messageListeners array of callbacks to handle incoming messages
     * @throws MqttException if there was an error registering the subscription.
     */
    @Override
    public void subscribeMessages(String[] topics, IMqttMessageListener[] messageListeners) throws MqttException{
        int[] qos = new int[topics.length];
        Arrays.fill(qos, this.getQos());
        this.subscribeMessages(topics, qos, messageListeners);
    }

    /**
     * Subscribe to a group of topics providing qos and messageListeners for each topic
     * @param topics String array of topics
     * @param qos int array of qos
     * @param messageListeners array of callbacks to handle incoming messages
     * @throws MqttException if there was an error registering the subscription.
     */
    @Override
    public void subscribeMessages(String[] topics, int[] qos, IMqttMessageListener[] messageListeners) throws MqttException{
        this.mqttClient.subscribe(topics, qos, messageListeners);
        log.info("Subscribed to provided Topics");
        for(int i=0; i<topics.length; i++){
            this.subscribedTuples.put(topics[i], new SubscribedTuple(topics[i], qos[i], messageListeners[i]));
        }
    }

    /**
     * Unsubscribe to a given topic
     * @param topic String mqtt topic
     * @throws MqttException if there was an error unregistering the subscription.
     */
    @Override
    public void unsubscribeMessage(String topic) throws MqttException{
        this.mqttClient.unsubscribe(topic);
        this.subscribedTuples.remove(topic);
    }

    /**
     * Unsubscribe to a group of topics
     * @param topics String array of topics to unsubscribe
     * @throws MqttException if there was an error unregistering the subscription.
     */
    @Override
    public void unsubscribeMessages(String[] topics) throws MqttException{
        this.mqttClient.unsubscribe(topics);
        for(String topic : topics){
            this.subscribedTuples.remove(topic);
        }
    }

    /**
     * Check if the MQTT Client is connected
     * @return boolean connected or not
     */
    @Override
    public boolean isConnected() {
        return this.mqttClient.isConnected();
    }

    /**
     * Disconnect MQTT Subscriber
     * @throws MqttException for problems encountered while disconnecting
     */
    @Override
    public void disconnect() throws MqttException{
        mqttClient.disconnect();
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
            this.mqttClient.publish("status/" + this.clientId, "connected".getBytes(), 2, true );
        } catch (MqttException e) {
            log.error(e.getMessage(), e);
        }
        if(this.subscribedTuples.size() != 0){
            for(SubscribedTuple tuple : this.subscribedTuples.values()){
                try {
                    this.mqttClient.subscribe(tuple.getTopic(), tuple.getQos(), tuple.getMessageListener());
                } catch (MqttException e) {
                    log.error(e.getMessage() + ", topic: " + tuple.getTopic(), e);
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
        log.info("Connection Lost");
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
    public void messageArrived(String topic, MqttMessage message) throws Exception{
        log.info("Topic: " + topic + ", " +
                "Message: " + message.toString());

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
        MemoryPersistence memoryPersistence = new MemoryPersistence();

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setWill("status/"+this.clientId, "disconnected".getBytes(), this.getQos(), true);
        if(!this.getUsername().trim().isEmpty()){
            mqttConnectOptions.setUserName(this.getUsername());
        }
        if(!this.getPassword().trim().isEmpty()){
            mqttConnectOptions.setPassword(this.getPassword().toCharArray());
        }

        try {
            this.mqttClient = new MqttClient(serverURL, this.clientId, memoryPersistence);
            this.mqttClient.setCallback(this);
            this.mqttClient.connect(mqttConnectOptions);
            Thread mqttClose = new Thread(() -> {
                try {
                    this.mqttClient.close();
                } catch (MqttException e) {
                    log.error(e.getMessage(), e);
                }
            });
            Runtime.getRuntime().addShutdownHook(mqttClose);

        } catch (MqttException e) {
            log.error(e.getMessage(), e);
        }

    }
}
