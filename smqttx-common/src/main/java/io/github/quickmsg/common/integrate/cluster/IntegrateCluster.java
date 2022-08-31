package io.github.quickmsg.common.integrate.cluster;

import io.github.quickmsg.common.integrate.IntegrateGetter;
import io.github.quickmsg.common.message.mqtt.ClusterMessage;
import io.github.quickmsg.common.message.mqtt.PublishMessage;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * @author luxurong
 */
public interface IntegrateCluster extends IntegrateGetter {

    /**
     * 获取集群节点信息
     *
     * @return node collection
     */
    Set<String> getClusterNode();


    /**
     * 获取其他集群节点信息
     *
     * @return other node collection
     */
    Set<String> getOtherClusterNode();


    /**
     * acquire local node id
     *
     * @return String
     */
    String getLocalNode();


    /**
     * 停止
     *
     * @return {@link Mono}
     */
    void shutdown();


    /**
     * 訂閱
     *
     * @param topic  mqttTopic
     */
    void listenTopic(String topic);

    /**
     * 停止
     *
     * @param topic  mqttTopic
     */
    void stopListenTopic(String topic);



    /**
     * 集群消息
     *
     * @param publishMessage {@link PublishMessage}
     */
    void sendCluster(PublishMessage publishMessage);

}
