package com.mohey.mqtt.core;
/**
 * @author Mohey El-Din Badr
 * @since 2020/12/28
 */

import com.mohey.mqtt.model.SubscribedTuple;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class MQTTSubscriber extends MQTTConfig implements MqttCallback, IMQTTSubscriber{
    
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
     * Called when an AUTH packet is received by the client.
     *
     * @param reasonCode The Reason code, can be Success (0), Continue authentication (24)
     *                   or Re-authenticate (25).
     * @param properties The {@link MqttProperties} to be sent, containing the
     *                   Authentication Method, Authentication Data and any required User
     */
    @Override
    public void authPacketArrived(int reasonCode, MqttProperties properties) {
        log.info("reasonCode: " + reasonCode +", " + "properties: " + properties);
    }
    

    /**
     * This method is called when the server gracefully disconnects from the client
     * by sending a disconnect packet, or when the TCP connection is lost due to a
     * network issue or if the client encounters an error.
     *
     * @param disconnectResponse a {@link MqttDisconnectResponse} containing relevant properties
     *                           related to the cause of the disconnection.
     */
    @Override
    public void disconnected(MqttDisconnectResponse disconnectResponse) {
        log.info("Connection lost: " + disconnectResponse.getReasonString());
    }

    /**
     * This method is called when an exception is thrown within the MQTT client. The
     * reasons for this may vary, from malformed packets, to protocol errors or even
     * bugs within the MQTT client itself. This callback surfaces those errors to
     * the application so that it may decide how best to deal with them.
     * <p>
     * For example, The MQTT server may have sent a publish message with an invalid
     * topic alias, the MQTTv5 specification suggests that the client should
     * disconnect from the broker with the appropriate return code, however this is
     * completely up to the application itself.
     *
     * @param exception - The exception thrown causing the error.
     */
    @Override
    public void mqttErrorOccurred(MqttException exception) {
        log.error(exception.getMessage(), exception);
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
     * acknowledgments have been received. For QoS 0 messages it is called once the
     * message has been handed to the network for delivery. For QoS 1 it is called
     * when PUBACK is received and for QoS 2 when PUBCOMP is received. The token
     * will be the same token as that returned when the message was published.
     *
     * @param token the delivery token associated with the message.
     */
    @Override
    public void deliveryComplete(IMqttToken token) {

    }


    @Override
    protected void config() {
        String serverURL = this.getTCP() + this.getUrl() + ":" + this.getPort();
        if(isHasSSl()){
            serverURL = this.getSSL() + this.getUrl() + ":" + this.getPort();
        }

        this.clientId = this.getClientId() + "_sub";
        MemoryPersistence memoryPersistence = new MemoryPersistence();

        MqttConnectionOptions mqttConnectOptions = new MqttConnectionOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanStart(true);
        if(!this.getAuthMethod().isBlank()){
            mqttConnectOptions.setAuthMethod(this.getAuthMethod());
        }
        if(!this.getAuthData().isBlank()){
            mqttConnectOptions.setAuthData(this.getAuthData().getBytes());
        }
        MqttMessage willMessage = new MqttMessage();
        willMessage.setPayload("disconnected".getBytes());
        willMessage.setQos(this.getQos());
        willMessage.setRetained(true);
        mqttConnectOptions.setWill("status/"+this.clientId,willMessage);

        if(!this.getUsername().isBlank()){
            mqttConnectOptions.setUserName(this.getUsername());
        }
        if(!this.getPassword().isBlank()){
            mqttConnectOptions.setPassword(this.getPassword().getBytes());
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
