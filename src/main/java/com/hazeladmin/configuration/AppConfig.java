package com.hazeladmin.configuration;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;

import com.hazelcast.client.config.ClientIcmpPingConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.management.TimedMemberState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AppConfig {

    @Bean
    public HazelcastInstance hazelcastInstance(){
        ClientConfig clientConfig=new ClientConfig();
        clientConfig.getNetworkConfig().addAddress("192.168.1.106");
        clientConfig.getNetworkConfig().setConnectionAttemptLimit(3);
        clientConfig.getNetworkConfig().setConnectionAttemptPeriod(10*1000);
        clientConfig.setInstanceName("hazelclient_0");
        HazelcastInstance hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);

        return hazelcastInstance;
    }

    @Bean
    public Map<String, Map<Long, TimedMemberState>> timedMemberStateMap(){
        return new HashMap<>();
    }
}
