package io.github.quickmsg.core.http.actors.resource;

import io.github.quickmsg.common.http.annotation.AllowCors;
import io.github.quickmsg.common.http.annotation.Header;
import io.github.quickmsg.common.http.annotation.Router;
import io.github.quickmsg.common.config.Configuration;
import io.github.quickmsg.common.http.enums.HttpType;
import io.github.quickmsg.common.http.HttpActor;
import io.github.quickmsg.common.utils.JacksonUtil;
import io.github.quickmsg.core.http.HttpConfiguration;
import io.github.quickmsg.core.http.model.LoginDo;
import io.github.quickmsg.core.http.model.LoginVm;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author luxurong
 */
@Router(value = "/auth/login", type = HttpType.POST,resource = true)
@Slf4j
@Header(key = "Content-Type", value = "application/json")
@AllowCors
public class LoginResourceActor implements HttpActor {

    @Override
    public Publisher<Void> doRequest(HttpServerRequest request, HttpServerResponse response, Configuration httpConfiguration) {
        return request
                .receive()
                .asString(StandardCharsets.UTF_8)
                .map(this.toJson(LoginDo.class))
                .doOnNext(loginDo -> {
                    Map<String, Object> res = new HashMap<>(2);
                    if (this.validateLogin(loginDo,(HttpConfiguration)httpConfiguration)) {
                        LoginVm loginVm = new LoginVm();
                        loginVm.setAccessToken("jhbsadhjbajhdbjhabdsjhahjbsdjhbajsdbjhahjsdb");
                        loginVm.setExpiresIn(System.currentTimeMillis() + 100000000000000L);
                        res.put("data", loginVm);
                        res.put("success",true);
                    } else {
                        res.put("success",false);
                    }

                    response.sendString(Mono.just(JacksonUtil.map2Json(res))).then().subscribe();
                }).then();

    }

    private boolean validateLogin(LoginDo loginDo, HttpConfiguration httpConfiguration) {
        String username  = Optional.ofNullable(httpConfiguration.getUsername()).orElse("smqtt");
        String password  = Optional.ofNullable(httpConfiguration.getPassword()).orElse("smqtt");
        return username.equals(loginDo.getUsername()) && password.equals(loginDo.getPassword());
    }
}
