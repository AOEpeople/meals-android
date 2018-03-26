package com.aoe.mealsapp;

public interface Consumer<T> {

    void accept(T t);
}
