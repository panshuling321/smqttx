package io.github.quickmsg.core.mqtt;

import io.github.quickmsg.common.channel.MqttChannel;
import io.github.quickmsg.common.message.SmqttMessage;
import io.github.quickmsg.common.transport.Transport;
import io.github.quickmsg.core.cluster.ClusterReceiver;
import io.netty.handler.codec.mqtt.MqttMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author luxurong
 */
@Getter
@Slf4j
public class MqttReceiveContext extends AbstractReceiveContext<MqttConfiguration> {


    private final ClusterReceiver clusterReceiver;

    public MqttReceiveContext(MqttConfiguration configuration, Transport<MqttConfiguration> transport) {
        super(configuration, transport, dslExecutor);
        this.clusterReceiver = new ClusterReceiver(this);
        clusterReceiver.registry();
    }

    public void apply(MqttChannel mqttChannel) {
        mqttChannel.registryDelayTcpClose()
                .getConnection()
                .inbound()
                .receiveObject()
                .cast(MqttMessage.class)
                .subscribe(mqttMessage -> this.accept(mqttChannel, new SmqttMessage<>(mqttMessage,System.currentTimeMillis(),Boolean.FALSE)));

    }


    @Override
    public void accept(MqttChannel mqttChannel, SmqttMessage<MqttMessage> mqttMessage) {
        log.info("accept channel] {} message {}", mqttChannel.getConnection(), mqttMessage);
        this.getProtocolAdaptor().chooseProtocol(mqttChannel, mqttMessage, this);
    }


}
