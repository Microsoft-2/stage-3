package com.microsoft2.bigdata.search.infrastructure.persistence;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;

public class HazelcastConfigFactory {
    public static Config createConfig() {
        Config config = new Config();
        
        config.setClusterName("search-cluster");

        MapConfig mapConfig = new MapConfig("inverted-index");
        mapConfig.setBackupCount(1);
        config.addMapConfig(mapConfig);
        
        String clusterMembers = System.getenv("CLUSTER_MEMBERS");
        if (clusterMembers != null && !clusterMembers.isEmpty()) {
            System.out.println("Configuring Hazelcast TCP/IP with members: " + clusterMembers);
            
            JoinConfig join = config.getNetworkConfig().getJoin();
            
            join.getMulticastConfig().setEnabled(false);
            
            join.getTcpIpConfig().setEnabled(true);
            
            for (String ip : clusterMembers.split(",")) {
                join.getTcpIpConfig().addMember(ip.trim());
            }
        } else {
            System.out.println("Variable CLUSTER_MEMBERS empty. Using Multicast by default (only local).");
        }
        
        return config;
    }
}
