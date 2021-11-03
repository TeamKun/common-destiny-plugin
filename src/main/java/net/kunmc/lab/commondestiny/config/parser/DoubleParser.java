package net.kunmc.lab.commondestiny.config.parser;

public class DoubleParser extends Parser<Double> {
    private final double min;
    private final double max;

    public DoubleParser(double min, double max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public Double parse(String str) {
        try {
            double value = Double.parseDouble(str);
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
