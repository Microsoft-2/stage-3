package com.microsoft2.bigdata.search.infrastructure.persistence;

// IMPORTANTE: Estos son los imports que te faltaban
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;

public class HazelcastConfigFactory {
    public static Config createConfig() {
        Config config = new Config();
        
        // Evitamos conflicto de nombres con el nombre del cluster por defecto
        config.setClusterName("search-cluster");

        // Leemos la lista de miembros del clúster desde variable de entorno
        // Ejemplo en docker-compose: CLUSTER_MEMBERS=192.168.1.10,192.168.1.20
        String clusterMembers = System.getenv("CLUSTER_MEMBERS");
        
        if (clusterMembers != null && !clusterMembers.isEmpty()) {
            System.out.println("🌍 Configurando Hazelcast TCP/IP con miembros: " + clusterMembers);
            
            // Accedemos a la configuración de red
            JoinConfig join = config.getNetworkConfig().getJoin();
            
            // 1. Desactivamos Multicast (que suele fallar entre PCs distintos o redes universitarias)
            join.getMulticastConfig().setEnabled(false);
            
            // 2. Activamos TCP/IP (Lista explícita de IPs)
            join.getTcpIpConfig().setEnabled(true);
            
            // Añadimos cada IP separada por comas
            for (String ip : clusterMembers.split(",")) {
                join.getTcpIpConfig().addMember(ip.trim());
            }
        } else {
            System.out.println("⚠️ Variable CLUSTER_MEMBERS vacía. Usando Multicast por defecto (solo local).");
        }
        
        return config;
    }
}