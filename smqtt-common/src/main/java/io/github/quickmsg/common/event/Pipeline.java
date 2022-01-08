package io.github.quickmsg.common.event;

import reactor.core.publisher.Flux;

import java.util.function.Consumer;

/**
 * @author luxurong
 */
public interface Pipeline  extends Consumer<Event> {

    <M extends Event> Flux<M> handle(Class<M > tClass);

}
