package io.github.quickmsg.common.message;

import io.github.quickmsg.common.utils.MqttMessageUtils;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author luxurong
 */
@Data
public class HttpPublishMessage {

    private String topic;

    private int qos;

    private boolean retain;

    private String message;

    private Map<String, Object> others;


    public MqttPublishMessage getPublishMessage() {
        return MqttMessageUtils.buildPub(
                false,
                MqttQoS.valueOf(qos),
                retain,
                1,
                topic,
                PooledByteBufAllocator.DEFAULT.buffer().writeBytes(message.getBytes(StandardCharsets.UTF_8)));
    }


}
