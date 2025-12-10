package com.microsoft2.bigdata.search;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class App {
    public static void main(String[] args) {
        System.out.println("Iniciando prueba de entorno...");
        
        // Esto debería arrancar una instancia de Hazelcast
        // Si Maven bajó bien las dependencias, esto funcionará.
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        
        System.out.println("¡Hazelcast ha arrancado correctamente!");
        
        // Apagamos para terminar la prueba
        hz.shutdown();
    }
}