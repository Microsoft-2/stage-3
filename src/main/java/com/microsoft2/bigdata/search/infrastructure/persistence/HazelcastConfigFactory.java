package com.microsoft2.bigdata.search.infrastructure.persistence;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;

public class HazelcastConfigFactory {
    public static Config createConfig() {
        Config config = new Config();
        
        // Leemos la lista de miembros del clúster desde variable de entorno
        // Ejemplo: "192.168.1.33,192.168.1.45"
        String clusterMembers = System.getenv("CLUSTER_MEMBERS");
        
        if (clusterMembers != null && !clusterMembers.isEmpty()) {
            System.out.println("🌍 Configurando Hazelcast TCP/IP con: " + clusterMembers);
            
            // Desactivamos Multicast (que suele fallar entre PCs distintos)
            JoinConfig join = config.getNetworkConfig().getJoin();
            join.getMulticastConfig().setEnabled(false);
            
            // Activamos TCP/IP 
            join.getTcpIpConfig().setEnabled(true);
            
            for (String ip : clusterMembers.split(",")) {
                join.getTcpIpConfig().addMember(ip);
            }
        }
        return config;
    }
}