package com.aisystems.sinu5oid.stochastic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class Engine {
    public Engine(
            Function<Integer, Double> meanFn,
            BiFunction<Integer, Integer, Double> correlationFn,
            int stepsCount,
            boolean useSafeMath
    ) {
        mFn = meanFn;
        kFn = correlationFn;
        this.stepsCount = stepsCount;
        this.useSafeMath = useSafeMath;

        random = new Random(System.currentTimeMillis());
        cacheMap = new HashMap<>();
    }

    public Supplier<Double[]> getImplementationGenerator() {
        Double[] devs = getDevs();
        Double[][] funcs = getFuncs();

        return () -> {
            Double[] randoms = new Double[stepsCount];
            for (int step = 0; step < stepsCount; step++) {
                randoms[step] = random.nextGaussian() * Math.sqrt(devs[step]);
            }

            return getConcreteImplementation(mFn, stepsCount, funcs, randoms);
        };
    }

    public Double[] getMeansObserved(Double[][] implementations) {
        Double[] res = new Double[stepsCount];
        Arrays.fill(res, 0d);

        for (Double[] doubles : implementations) {
            for (int step = 0; step < stepsCount; step++) {
                res[step] += doubles[step];
            }
        }

        for (int step = 0; step < stepsCount; step++) {
            res[step] /= implementations.length;
        }

        return res;
    }

    public Double[] getMeansTheoretical() {
        Double[] res = new Double[stepsCount];
        for (int step = 0; step < stepsCount; step++) {
            res[step] = mFn.apply(step);
        }

        return res;
    }

    public Double[][] getFuncsObserved(Double[][] implementations) {
        Double[][] res = new Double[stepsCount][stepsCount];

        for (int i = 0; i < stepsCount; i += 1) {
            for (int j = 0; j < stepsCount; j += 1) {
                double sij = 0d;
                double si = 0d;
                double sj = 0d;

                for (int k = 0; k < stepsCount; k += 1) {
                    sij += implementations[k][i] *implementations[k][j];
                    si += implementations[k][i];
                    sj += implementations[k][j];
                }

                res[i][j] = (sij - (si * sj) / stepsCount) / (stepsCount - 1);
            }
        }

        return res;
    }

    public Double[][] getFuncsTheoretical() {
        Double[][] res = new Double[stepsCount][];
        for (int step = 0; step < stepsCount; step++) {
            res[step] = new Double[stepsCount];
        }

        for (int step = 0; step < stepsCount; step++) {
            for (int innerStep = 0; innerStep < stepsCount; innerStep++) {
                res[step][innerStep] = kFn.apply(step, innerStep);
            }
        }

        return res;
    }

    private static Double[] getConcreteImplementation(Function<Integer, Double> mFn,
                                                      int stepsCount,
                                                      Double[][] funcs,
                                                      Double[] randoms) {
        Double[] res = new Double[stepsCount];

        for (int i = 0; i < stepsCount; i++) {
            Double impl = mFn.apply(i);

            for (int j = 0; j <= i; j++) {
                impl += randoms[j] * funcs[j][i];
            }

            res[i] = impl;
        }

        return res;
    }

    private Double[] getDevs() {
        Double[] res = new Double[stepsCount];
        for (int stepIdx = 0; stepIdx < stepsCount; stepIdx++) {
            res[stepIdx] = getDev(stepIdx);
        }

        return res;
    }

    private Double[][] getFuncs() {
        Double[][] res = new Double[stepsCount][stepsCount];

        for (int i = 0; i < stepsCount; i++) {
            for (int j = 0; j < stepsCount; j++) {
                res[i][j] = getFunc(i, j);
            }
        }

        return res;
    }

    private Double getDev(int i) {
        String cacheKey = "dev_" + i;
        Double cached = cacheMap.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Double kVal = kFn.apply(i, i);
        Double intermediate = 0d;

        for (int k = 0; k < i; k++) {
            intermediate += (Math.pow(getFunc(k, i), 2) * getDev(k));
        }

        double res = kVal - intermediate;

        if (useSafeMath && (intermediate > kVal || Double.isNaN(res))) {
            res = 0d;
        }

        cacheMap.put(cacheKey, res);
        return res;
    }

    private Double getFunc(int i, int j) {
        String cacheKey = "func_" + i + "_" + j;
        Double cached = cacheMap.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        double res;
        if (i == j) {
            res = 1;
        } else if (i > j) {
            res = 0;
        } else {
            if (useSafeMath && getDev(i) == 0) {
                res = 0;
            } else {
                Double dividend = kFn.apply(i, j);

                for (int k = 0; k < i; k++) {
                    dividend -= getFunc(k, i) * getFunc(k, j) * getDev(k);
                }

                res = dividend / getDev(i);
            }
        }

        cacheMap.put(cacheKey, res);
        return res;
    }

    private final Function<Integer, Double> mFn;
    private final BiFunction<Integer, Integer, Double> kFn;
    private final int stepsCount;
    private final boolean useSafeMath;

    private final Random random;
    private final HashMap<String, Double> cacheMap;
}
