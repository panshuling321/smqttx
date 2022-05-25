package io.github.quickmsg.core.http.actors.source;

import io.github.quickmsg.common.config.Configuration;
import io.github.quickmsg.common.http.annotation.AllowCors;
import io.github.quickmsg.common.http.annotation.Header;
import io.github.quickmsg.common.http.annotation.Router;
import io.github.quickmsg.common.http.enums.HttpType;
import io.github.quickmsg.common.rule.source.SourceDefinition;
import io.github.quickmsg.core.http.AbstractHttpActor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.nio.charset.StandardCharsets;


/**
 * 数据源删除
 *
 * @author zhaopeng
 */
@Router(value = "/smqtt/sourceDelete", type = HttpType.POST)
@Slf4j
@Header(key = "Content-Type", value = "application/json")
@AllowCors
public class SourceDeleteActor extends AbstractHttpActor {


    @Override
    public Publisher<Void> doRequest(HttpServerRequest request, HttpServerResponse response, Configuration httpConfiguration) {
        return request.receive().asString(StandardCharsets.UTF_8).map(this.toList(SourceDefinition.class)).doOnNext(message -> {
            // todo
            log.info("http request url {} body {}", request.path(), message);
        }).then(response.sendString(Mono.just("success")).then());
    }
}
