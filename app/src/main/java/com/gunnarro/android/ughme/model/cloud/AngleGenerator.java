package com.gunnarro.android.ughme.model.cloud;

import java.util.Random;

public class AngleGenerator {

    private static final Random RANDOM = new Random();
    private final int steps;
    private final double[] thetas;

    public AngleGenerator() {
        steps = 3;
        thetas = calculateThetas(-90, 90);
    }

    public double randomNext() {
        return thetas[RANDOM.nextInt(steps)];
    }

    private double[] calculateThetas(final double to, final double from) {
        final double stepSize = (to - from) / (steps - 1);
        final double[] thetas = new double[steps];
        for (int i = 0; i < steps; i++) {
            thetas[i] = Math.toRadians(from + (i * stepSize));
        }
        return thetas;
    }

}
