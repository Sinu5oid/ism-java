package com.aisystems.sinu5oid.markov;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class RandomGenerator implements IStepGenerator {
    public RandomGenerator(@NotNull double[] probRow) {
        this.segments = new Segment[probRow.length];

        double leftBound;
        double rightBound = 0d;

        for (int i = 0; i < probRow.length; i++) {
            if (probRow[i] == 0) {
                continue;
            }

            leftBound = rightBound;
            rightBound = leftBound + probRow[i];

            segments[i] = new Segment(leftBound, rightBound, i);
        }

        random = new Random();
    }

    public int next() {
        double rnd = random.nextDouble();

        for (Segment s : segments) {
            if (s == null) {
                continue;
            }

            if (s.leftBound <= rnd && s.rightBound > rnd) {
                return s.value;
            }
        }

        return segments[segments.length - 1].value;
    }

    private static class Segment {
        Segment(double leftBound, double rightBound, int value) {
            this.leftBound = leftBound;
            this.rightBound = rightBound;
            this.value = value;
        }

        private final double leftBound;
        private final double rightBound;
        private final int value;
    }

    private final Segment[] segments;
    private final Random random;
}
