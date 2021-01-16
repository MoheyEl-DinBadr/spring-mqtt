package com.mohey.mqtt.core;

/**
 * @author Mohey El-Din Badr
 * @since 2020/12/29
 */

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MQTTPublisher extends MQTTConfig implements MqttCallback, IMQTTPublisher {

    private MqttAsyncClient mqttClient;

    private static MQTTPublisher instance;

    private String clientId;

    private MQTTPublisher() {
        instance = this;
    }

    /**
     * If used must be sure that the bean has been created
     * @return returns instance of the MQTTPublisher
     */
    public static MQTTPublisher getInstance(){
        return instance;
    }

    /**
     * The method is used to publish a message using topic, and parameters with the default qos set at the enviroment
     * @param topic The MQTT topic to publish the message on
     * @param message String of the message to be delivered
     * @throws MqttException for other errors encountered while publishing the message.
     */
    @Override
    public void publishMessage(String topic, String message) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        mqttMessage.setQos(this.getQos());
        this.mqttClient.publish(topic, mqttMessage);
    }

    /**
     *
     * @param topic String MQTT topic to publish the message on
     * @param message MqttMessage to be published
     * @throws MqttException for other errors encountered while publishing the message.
     */
    @Override
    public void publishMessage(String topic, MqttMessage message) throws MqttException {
        this.mqttClient.publish(topic, message);
    }

    /**
     *
     * @param topic to deliver the message to, for example "finance/stock/ibm".
     * @param message string message to be used as payload
     * @param qos the Quality of Service to deliver the message at. Valid values are 0, 1 or 2.
     * @param retain whether or not this message should be retained by the server.
     * @throws MqttException for other errors encountered while publishing the message.
     */
    @Override
    public void publishMessage(String topic, String message, int qos, boolean retain) throws MqttException {
        this.mqttClient.publish(topic, message.getBytes(), qos, retain);
    }

    /**
     * @return boolean if the client is connected or not
     */
    @Override
    public boolean isConnected() {
        return this.mqttClient.isConnected();
    }

    /**
     * disconnects the mqtt client
     * @throws MqttException for problems encountered while disconnecting
     */
    @Override
    public void disconnect() throws MqttException{
        this.mqttClient.disconnect();
    }

    @Override
    protected void config() {
        String serverURL = this.getTCP() + this.getUrl() + ":" + this.getPort();
        if(isHasSSl()){
            serverURL = serverURL.replace(this.getTCP(), this.getSSL());
        }
        this.clientId = this.getClientId() + "_pub";

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
            log.error(e.getMessage(), e);
        }
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
        log.info(disconnectResponse.getReasonString());
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
    public void messageArrived(String topic, MqttMessage message) throws Exception {

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
        log.info("Delivery Completed");
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
            log.error(e.getMessage(), e);
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
}
