package io.github.quickmsg.core.http.actors.acl;

import io.github.quickmsg.common.acl.AclAction;
import io.github.quickmsg.common.acl.model.PolicyModel;
import io.github.quickmsg.common.config.Configuration;
import io.github.quickmsg.common.context.ContextHolder;
import io.github.quickmsg.common.http.annotation.AllowCors;
import io.github.quickmsg.common.http.annotation.Header;
import io.github.quickmsg.common.http.annotation.Router;
import io.github.quickmsg.common.http.enums.HttpType;
import io.github.quickmsg.core.http.AbstractHttpActor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.nio.charset.StandardCharsets;

/**
 * @author luxurong
 */

@Router(value = "/smqtt/acl/policy/add", type = HttpType.POST)
@Slf4j
@Header(key = "Content-Type", value = "application/json")
@AllowCors
public class AclAddPolicyActor extends AbstractHttpActor {

    @Override
    public Publisher<Void> doRequest(HttpServerRequest request, HttpServerResponse response, Configuration configuration) {
        return request
                .receive()
                .asString(StandardCharsets.UTF_8)
                .map(this.toJson(PolicyModel.class))
                .doOnNext(policyModel -> {
                            if (policyModel.getAction() == AclAction.ALL) {
                                ContextHolder.getReceiveContext().getAclManager().add
                                        (policyModel.getSubject(), policyModel.getSource(), AclAction.SUBSCRIBE,policyModel.getAclType());
                                ContextHolder.getReceiveContext().getAclManager().add(policyModel.getSubject(), policyModel.getSource(), AclAction.PUBLISH,policyModel.getAclType());
                            } else {
                                ContextHolder.getReceiveContext().getAclManager().add(policyModel.getSubject(), policyModel.getSource(), policyModel.getAction(),policyModel.getAclType());
                            }
                        }
                ).then(response.sendString(Mono.just("success")).then());
    }
}
