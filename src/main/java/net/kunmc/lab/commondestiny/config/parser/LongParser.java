package net.kunmc.lab.commondestiny.config.parser;

public class LongParser extends Parser<Long> {
    private final long min;
    private final long max;

    public LongParser(long min, long max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public Long parse(String str) {
        try {
            long value = Long.parseLong(str);
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
