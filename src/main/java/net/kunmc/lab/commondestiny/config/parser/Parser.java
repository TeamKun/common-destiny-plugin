package net.kunmc.lab.commondestiny.config.parser;

public abstract class Parser<T> {
    public abstract T parse(String str);
}
