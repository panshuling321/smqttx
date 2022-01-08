package io.github.quickmsg.core.protocol;

import io.github.quickmsg.common.channel.MqttChannel;
import io.github.quickmsg.common.context.ContextHolder;
import io.github.quickmsg.common.context.ReceiveContext;
import io.github.quickmsg.common.event.Event;
import io.github.quickmsg.common.event.acceptor.SubscribeEvent;
import io.github.quickmsg.common.integrate.SubscribeTopic;
import io.github.quickmsg.common.integrate.msg.IntegrateMessages;
import io.github.quickmsg.common.integrate.topic.IntegrateTopics;
import io.github.quickmsg.common.message.mqtt.RetryMessage;
import io.github.quickmsg.common.message.mqtt.SubscribeMessage;
import io.github.quickmsg.common.metric.CounterType;
import io.github.quickmsg.common.metric.MetricManagerHolder;
import io.github.quickmsg.common.protocol.Protocol;
import io.github.quickmsg.common.utils.EventMsg;
import io.github.quickmsg.common.utils.MqttMessageUtils;
import io.netty.handler.codec.mqtt.MqttQoS;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

import java.util.stream.Collectors;

/**
 * @author luxurong
 */
public class SubscribeProtocol implements Protocol<SubscribeMessage> {


    @Override
    public Mono<Event> parseProtocol(SubscribeMessage message, MqttChannel mqttChannel, ContextView contextView) {
        MetricManagerHolder.metricManager.getMetricRegistry().getMetricCounter(CounterType.SUBSCRIBE_EVENT).increment();
        return Mono.fromRunnable(() -> {
                    ReceiveContext<?> receiveContext = contextView.get(ReceiveContext.class);
                    IntegrateTopics<SubscribeTopic> topics = receiveContext.getIntegrate().getTopics();
                    IntegrateMessages messages = receiveContext.getIntegrate().getMessages();
                    message.getSubscribeTopics()
                            .forEach(subscribeTopic -> {
                                this.loadRetainMessage(messages, mqttChannel, subscribeTopic);
                                topics.registryTopic(subscribeTopic.getTopicFilter(), subscribeTopic.setMqttChannel(mqttChannel));
                            });
                }).then(mqttChannel.write(
                        MqttMessageUtils.buildSubAck(
                                message.getMessageId(),
                                message.getSubscribeTopics()
                                        .stream()
                                        .map(subscribeTopic -> subscribeTopic.getQoS().value())
                                        .collect(Collectors.toList()))))
                .thenReturn(buildEvent(message, mqttChannel));
    }

    @Override
    public Class<SubscribeMessage> getClassType() {
        return SubscribeMessage.class;
    }

    private SubscribeEvent buildEvent(SubscribeMessage message, MqttChannel mqttChannel) {
        return new SubscribeEvent(
                mqttChannel.getConnectMessage().getClientId(),
                message.getSubscribeTopics(),
                System.currentTimeMillis());
    }

    private void loadRetainMessage(IntegrateMessages messages, MqttChannel mqttChannel, SubscribeTopic topic) {
        messages.getRetainMessage(topic.getTopicFilter())
                .forEach(retainMessage ->{
                    MqttQoS minQos = topic.minQos(MqttQoS.valueOf(retainMessage.getQos()));
                    int messageId = 0;
                    if(minQos.value()>0){
                        messageId = mqttChannel.generateMessageId();
                        RetryMessage retryMessage = new RetryMessage(messageId,System.currentTimeMillis(), false, retainMessage.getTopic(), MqttQoS.valueOf(retainMessage.getQos()), retainMessage.getBody(), mqttChannel, ContextHolder.getReceiveContext());
                        doRetry(mqttChannel.generateRetryId(messageId),5,retryMessage);
                    }
                    mqttChannel.write(retainMessage.toPublishMessage(messageId)).subscribe();
                });
    }


}
