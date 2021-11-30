package io.github.quickmsg.core.protocol;

import io.github.quickmsg.common.channel.MqttChannel;
import io.github.quickmsg.common.event.Event;
import io.github.quickmsg.common.message.mqtt.PingMessage;
import io.github.quickmsg.common.protocol.Protocol;
import io.github.quickmsg.common.utils.EventMsg;
import io.github.quickmsg.common.utils.MqttMessageUtils;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

/**
 * @author luxurong
 */
public class PingProtocol implements Protocol<PingMessage> {

    @Override
    public Mono<Event> parseProtocol(PingMessage message, MqttChannel mqttChannel, ContextView contextView) {
        return mqttChannel.write(MqttMessageUtils.buildPongMessage(), false)
                .then(Mono.fromSupplier(() -> build(EventMsg.PING_MESSAGE, mqttChannel.getClientIdentifier(), 0)));
    }
}
