package com.microsoft2.bigdata.search.domain.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Document implements Serializable {
    private final String id;
    private final String content;
    
    public Document(String id, String content){
        this.id = id;
        this.content = content;
    }

    public String getId(){
        return id;
    }

    public String getContent(){
        return content;
    }

    //Dividimos el documento en PALABRAS (tokens)
    public Set<String> tokenize() {
        if (content == null || content.isEmpty()) return new HashSet<>();
        // Divide por espacios y convierte a minúsculas
        String[] tokens = content.toLowerCase().split("\\W+");
        return new HashSet<>(Arrays.asList(tokens));
    }
}
