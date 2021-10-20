package io.github.quickmsg.core.mqtt;

import io.github.quickmsg.common.auth.PasswordAuthentication;
import io.github.quickmsg.common.channel.ChannelRegistry;
import io.github.quickmsg.common.channel.MqttChannel;
import io.github.quickmsg.common.channel.traffic.TrafficHandlerLoader;
import io.github.quickmsg.common.cluster.ClusterRegistry;
import io.github.quickmsg.common.config.AbstractConfiguration;
import io.github.quickmsg.common.config.Configuration;
import io.github.quickmsg.common.context.ReceiveContext;
import io.github.quickmsg.common.enums.ChannelStatus;
import io.github.quickmsg.common.enums.Event;
import io.github.quickmsg.common.message.HeapMqttMessage;
import io.github.quickmsg.common.message.MessageRegistry;
import io.github.quickmsg.common.message.EventRegistry;
import io.github.quickmsg.common.message.SmqttMessage;
import io.github.quickmsg.common.protocol.ProtocolAdaptor;
import io.github.quickmsg.common.rule.DslExecutor;
import io.github.quickmsg.common.topic.TopicRegistry;
import io.github.quickmsg.common.transport.Transport;
import io.github.quickmsg.core.cluster.InJvmClusterRegistry;
import io.github.quickmsg.core.mqtt.traffic.CacheTrafficHandlerLoader;
import io.github.quickmsg.core.mqtt.traffic.LazyTrafficHandlerLoader;
import io.github.quickmsg.core.spi.*;
import io.github.quickmsg.dsl.RuleDslParser;
import io.github.quickmsg.rule.source.SourceManager;
import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;
import reactor.netty.resources.LoopResources;

import java.util.Map;
import java.util.Optional;

/**
 * @author luxurong
 */
@Getter
@Setter
@Slf4j
public abstract class AbstractReceiveContext<T extends Configuration> implements ReceiveContext<T> {

    private T configuration;

    private LoopResources loopResources;

    private Transport<T> transport;

    private final ProtocolAdaptor protocolAdaptor;

    private final ChannelRegistry channelRegistry;

    private final TopicRegistry topicRegistry;

    private final MessageRegistry messageRegistry;

    private final PasswordAuthentication passwordAuthentication;

    private final ClusterRegistry clusterRegistry;

    private final EventRegistry eventRegistry;

    private final DslExecutor dslExecutor;

    private final TrafficHandlerLoader trafficHandlerLoader;


    public AbstractReceiveContext(T configuration, Transport<T> transport) {
        AbstractConfiguration abstractConfiguration = castConfiguration(configuration);
        RuleDslParser ruleDslParser = new RuleDslParser(abstractConfiguration.getRuleChainDefinitions());
        this.configuration = configuration;
        this.transport = transport;
        this.dslExecutor = ruleDslParser.parseRule();
        this.eventRegistry = eventRegistry();
        this.protocolAdaptor = protocolAdaptor();
        this.channelRegistry = channelRegistry();
        this.topicRegistry = topicRegistry();
        this.loopResources = LoopResources.create("smqtt-cluster-io", configuration.getBossThreadSize(), configuration.getWorkThreadSize(), true);
        this.trafficHandlerLoader = trafficHandlerLoader();
        this.messageRegistry = messageRegistry();
        this.clusterRegistry = clusterRegistry();
        this.passwordAuthentication = basicAuthentication();
        this.channelRegistry.startUp(abstractConfiguration.getEnvironmentMap());
        this.messageRegistry.startUp(abstractConfiguration.getEnvironmentMap());
        Optional.ofNullable(abstractConfiguration.getSourceDefinitions())
                .ifPresent(sourceDefinitions -> sourceDefinitions.forEach(SourceManager::loadSource));
    }

    private TrafficHandlerLoader trafficHandlerLoader() {
        if (configuration.getGlobalReadWriteSize() == null && configuration.getChannelReadWriteSize() == null) {
            return new CacheTrafficHandlerLoader(new GlobalTrafficShapingHandler(this.loopResources.onServer(true).next()));
        } else if (configuration.getChannelReadWriteSize() == null) {
            String[] limits = configuration.getGlobalReadWriteSize().split(",");
            return new CacheTrafficHandlerLoader(new GlobalTrafficShapingHandler(this.loopResources.onServer(true),
                    Long.parseLong(limits[1]),
                    Long.parseLong(limits[0])));
        } else if (configuration.getGlobalReadWriteSize() == null) {
            String[] limits = configuration.getChannelReadWriteSize().split(",");
            return new LazyTrafficHandlerLoader(() -> new GlobalTrafficShapingHandler(this.loopResources.onServer(true),
                    Long.parseLong(limits[1]),
                    Long.parseLong(limits[0])));
        } else {
            String[] globalLimits = configuration.getGlobalReadWriteSize().split(",");
            String[] channelLimits = configuration.getChannelReadWriteSize().split(",");
            return new CacheTrafficHandlerLoader(new GlobalChannelTrafficShapingHandler(
                    this.loopResources.onServer(true),
                    Long.parseLong(globalLimits[1]),
                    Long.parseLong(globalLimits[0]),
                    Long.parseLong(channelLimits[1]),
                    Long.parseLong(channelLimits[0])));
        }
    }


    private EventRegistry eventRegistry() {
        return Event::sender;
    }

    private MessageRegistry messageRegistry() {
        return Optional.ofNullable(MessageRegistry.INSTANCE)
                .orElse(new DefaultMessageRegistry());
    }

    private PasswordAuthentication basicAuthentication() {
        AbstractConfiguration abstractConfiguration = castConfiguration(configuration);
        return Optional.ofNullable(PasswordAuthentication.INSTANCE)
                .orElse(abstractConfiguration.getReactivePasswordAuth());
    }

    private ChannelRegistry channelRegistry() {
        return Optional.ofNullable(ChannelRegistry.INSTANCE)
                .orElse(new DefaultChannelRegistry());
    }

    private TopicRegistry topicRegistry() {
        return Optional.ofNullable(TopicRegistry.INSTANCE)
                .orElse(new DefaultTopicRegistry());
    }

    private ProtocolAdaptor protocolAdaptor() {
        return Optional.ofNullable(ProtocolAdaptor.INSTANCE)
                .orElse(new DefaultProtocolAdaptor(Schedulers.newBoundedElastic(configuration.getBusinessThreadSize(), configuration.getBusinessQueueSize(), "business-io")))
                .proxy();
    }

    private ClusterRegistry clusterRegistry() {
        return Optional.ofNullable(ClusterRegistry.INSTANCE)
                .orElse(new InJvmClusterRegistry());
    }


    private AbstractConfiguration castConfiguration(T configuration) {
        return (AbstractConfiguration) configuration;
    }

}
