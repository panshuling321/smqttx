package io.github.quickmsg.common.utils;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * @author luxurong
 */
@Slf4j
public class MessageUtils {

    public static void safeRelease(MqttMessage mqttMessage) {
        if (mqttMessage.payload() instanceof ByteBuf) {
            ByteBuf byteBuf = ((ByteBuf) mqttMessage.payload());
            int count = byteBuf.refCnt();
            if (count > 0) {
                byteBuf.release(count);
                if (log.isDebugEnabled()) {
                    log.info("netty success release mqttMessage {} count {} ", byteBuf, count);
                }
            }
        }
    }

    public static void safeRelease(MqttMessage mqttMessage, Integer count) {
        if (mqttMessage.payload() instanceof ByteBuf) {
            ByteBuf byteBuf = ((ByteBuf) mqttMessage.payload());
            if (count > 0) {
                byteBuf.release(count);
                if (log.isDebugEnabled()) {
                    log.info("netty success release mqttMessage {} count {} ", byteBuf, count);
                }
            }
        }
    }

    public static void safeRelease(ByteBuf buf) {
        int count = buf.refCnt();
        if (count > 0) {
            buf.release(count);
            if (log.isDebugEnabled()) {
                log.info("netty success release byteBuf {} count {} ", buf, count);
            }
        }
    }

    public static void safeRelease(ByteBuf buf, Integer count) {
        if (count > 0) {
            buf.release(count);
            if (log.isDebugEnabled()) {
                log.info("netty success release byteBuf {} count {} ", buf, count);
            }
        }
    }


    /**
     * 获取释放消息字节数组
     *
     * @param byteBuf 消息ByteBuf
     * @return 字节数组
     */
    public static byte[] copyReleaseByteBuf(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        byteBuf.resetReaderIndex();
        return bytes;
    }


    /**
     * 获取释放消息字节数组
     *
     * @param byteBuf 消息ByteBuf
     * @return 字节数组
     */
    public static byte[] copyByteBuf(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.resetReaderIndex();
        byteBuf.readBytes(bytes);
        byteBuf.resetReaderIndex();
        return bytes;
    }

    /**
     * 获取释放消息字节数组
     *
     * @param byteBuf 消息ByteBuf
     * @return 字节数组
     */
    public static byte[] readByteBuf(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.resetReaderIndex();
        byteBuf.readBytes(bytes);
        byteBuf.resetReaderIndex();
        return bytes;
    }


}
