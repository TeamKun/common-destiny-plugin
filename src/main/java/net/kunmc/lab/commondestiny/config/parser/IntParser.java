package net.kunmc.lab.commondestiny.config.parser;

public class IntParser extends Parser<Integer> {
    private final int min;
    private final int max;

    public IntParser(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public Integer parse(String str) {
        try {
            int value = Integer.parseInt(str);
            if (min <= value && value <= max) {
                return value;
            } else {
                return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
