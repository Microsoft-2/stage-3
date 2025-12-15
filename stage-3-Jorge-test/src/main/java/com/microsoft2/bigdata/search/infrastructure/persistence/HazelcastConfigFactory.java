package com.microsoft2.bigdata.search.infrastructure.persistence;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig; // Nuevo Import

public class HazelcastConfigFactory {
    public static Config createConfig() {
        Config config = new Config();
        
        // Evitamos conflicto de nombres con el nombre del cluster por defecto
        config.setClusterName("search-cluster");

        // --- 1. CONFIGURACIÓN DE REPLICACIÓN DEL ÍNDICE (BACKUPS) ---
        // Asumiendo que el mapa del índice invertido se llama "inverted-index"
        MapConfig mapConfig = new MapConfig("inverted-index");
        // Establecer el número de copias de seguridad. 1 es lo mínimo para tolerancia a fallos.
        mapConfig.setBackupCount(1);
        config.addMapConfig(mapConfig);
        
        // --- 2. CONFIGURACIÓN DE CLUSTERING (TCP/IP) ---
        String clusterMembers = System.getenv("CLUSTER_MEMBERS");
        // ... (El resto de tu lógica de descubrimiento es correcta y se mantiene)
        //
        if (clusterMembers != null && !clusterMembers.isEmpty()) {
            System.out.println("Configuring Hazelcast TCP/IP with members: " + clusterMembers);
            
            // Accedemos a la configuración de red
            JoinConfig join = config.getNetworkConfig().getJoin();
            
            // 1. Desactivamos Multicast 
            join.getMulticastConfig().setEnabled(false);
            
            // 2. Activamos TCP/IP
            join.getTcpIpConfig().setEnabled(true);
            
            // Añadimos cada peer de Docker Compose (indexer1, indexer2, etc.)
            for (String ip : clusterMembers.split(",")) {
                join.getTcpIpConfig().addMember(ip.trim());
            }
        } else {
            System.out.println("Variable CLUSTER_MEMBERS empty. Using Multicast by default (only local).");
        }
        
        return config;
    }
}
