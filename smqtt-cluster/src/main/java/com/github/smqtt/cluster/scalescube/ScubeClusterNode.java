package com.github.smqtt.cluster.scalescube;

import com.github.smqtt.cluster.base.ClusterNode;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author luxurong
 * @date 2021/4/12 17:14
 * @description
 */
@Data
@AllArgsConstructor
public class ScubeClusterNode implements ClusterNode {

    private Object member;
}
