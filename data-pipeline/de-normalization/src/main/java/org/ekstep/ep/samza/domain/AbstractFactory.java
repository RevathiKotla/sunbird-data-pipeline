package org.ekstep.ep.samza.domain;

public interface AbstractFactory<T> {
    T getInstance(String type);
}

