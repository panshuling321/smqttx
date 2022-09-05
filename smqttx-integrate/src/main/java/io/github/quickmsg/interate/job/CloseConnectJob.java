package io.github.quickmsg.interate.job;

import cn.hutool.core.collection.CollectionUtil;
import io.github.quickmsg.common.channel.MqttChannel;
import io.github.quickmsg.common.context.ContextHolder;
import io.github.quickmsg.common.integrate.SubscribeTopic;
import io.github.quickmsg.common.integrate.channel.IntegrateChannels;
import io.github.quickmsg.common.integrate.job.JobClosure;
import io.github.quickmsg.common.integrate.topic.IntegrateTopics;

import java.util.ArrayList;

/**
 * @author luxurong
 */
public class CloseConnectJob implements JobClosure<String,Boolean> {

    @Override
    public String getJobName() {
        return "close-connect";
    }

    @Override
    public Boolean isBroadcast() {
        return true;
    }


    @Override
    public Boolean apply(String clientId) {
        IntegrateChannels channels = ContextHolder.getReceiveContext()
                    .getIntegrate().getChannels();
        IntegrateTopics<SubscribeTopic> integrateTopics = ContextHolder.getReceiveContext()
                    .getIntegrate().getTopics();
        MqttChannel mqttChannel = channels.get(clientId);
        if (mqttChannel != null) {
            mqttChannel.close();
        }
        return mqttChannel != null;
    }
}
