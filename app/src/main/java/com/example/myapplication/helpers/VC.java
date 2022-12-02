package com.example.myapplication.helpers;

public class VC<T> {
    public T v = null;

    public VC() {

    }

    public VC(T v) {
        this.v = v;
    }

    public void set(T v) {
        this.v = v;
    }

    public T get() {
        return this.v;
    }
}
