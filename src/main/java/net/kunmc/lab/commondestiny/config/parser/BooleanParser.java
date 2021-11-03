package net.kunmc.lab.commondestiny.config.parser;

public class BooleanParser extends Parser<Boolean> {
    @Override
    public Boolean parse(String str) {
        if (str.equalsIgnoreCase("true")) {
            return true;
        } else if (str.equalsIgnoreCase("false")) {
            return false;
        } else {
            return null;
        }
    }
}
