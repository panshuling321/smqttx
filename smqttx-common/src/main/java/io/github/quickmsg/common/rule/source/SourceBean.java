package io.github.quickmsg.common.rule.source;

import io.github.quickmsg.common.spi.loader.DynamicLoader;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author luxurong
 */
public interface SourceBean {


    List<SourceBean> SOURCE_BEAN_LIST = DynamicLoader.findAll(SourceBean.class)
            .collect(Collectors.toList());


    /**
     * 是否支持source
     *
     * @param source {@link Source}
     * @return Boolean
     */
    Boolean support(Source source);


    /**
     * 启动source
     *
     * @param sourceParam 请求参数
     * @return Boolean
     */
    Boolean bootstrap(Map<String, Object> sourceParam);


    /**
     * 转发数据
     *
     * @param object {@link Map}
     */
    void transmit(Object object);

    /**
     * 关闭资源
     */
    void close();

}
