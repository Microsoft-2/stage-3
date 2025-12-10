package com.microsoft2.bigdata.search.domain.ports;

public interface BookProvider {
    String getBookText(String bookId);
}