package com.aisystems.sinu5oid.stochastic;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class Runner {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger(com.aisystems.sinu5oid.stochastic.Runner.class.getName());

        double m = 1;
        int n = 400;
        double h = 0.1;
        int N = 10000;

        Function<Integer, Double> mFn = (step) -> m;
        BiFunction<Integer, Integer, Double> kFn = (i, j) -> 1 / (1 - 0.5 * Math.cos(getT(i, h) - getT(j, h)));

        logger.info("creating implementation engine");
        Engine engine = new Engine(mFn, kFn, n, true);
        Supplier<Double[]> generator = engine.getImplementationGenerator();
        logger.info("engine created");

        logger.info("collecting implementations");
        Double[][] implementations = new Double[N][];
        for (int i = 0; i < N; i++) {
            implementations[i] = generator.get();
        }
        logger.info("implementations collected");

        logger.info("computing theoretical means");
        Double[] meansTheoretical = engine.getMeansTheoretical();
        logger.info("theoretical means computed");

        logger.info("computing theoretical functions");
        Double[][] functionsTheoretical = engine.getFuncsTheoretical();
        logger.info("theoretical functions computed");

        logger.info("computing observed means");
        Double[] meansObserved = engine.getMeansObserved(implementations);
        logger.info("observed means computed");

        logger.info("computing observed functions");
        Double[][] functionsObserved = engine.getFuncsObserved(implementations);
        logger.info("observed functions computed");

        System.out.println(Arrays.toString(meansTheoretical));
        System.out.println(Arrays.toString(meansObserved));
        System.out.println(Arrays.deepToString(functionsTheoretical));
        System.out.println(Arrays.deepToString(functionsObserved));

        logger.info("plotting charts");
        showCharts(fromArray(meansObserved, n, h),
                fromArray(meansTheoretical, n, h),
                fromArray(functionsObserved[0], n, h),
                fromArray(functionsTheoretical[0], n, h),
                new ArrayList<>(Arrays.asList(fromArray(implementations[0], n, h),
                        fromArray(implementations[1], n, h),
                        fromArray(implementations[2], n, h))));
        logger.info("finished plotting charts");
    }

    static double getT(int t, double h) {
        return t * h;
    }

    static void showCharts(ArrayList<DoublePoint> meansObserved,
                           ArrayList<DoublePoint> meansTheoretical,
                           ArrayList<DoublePoint> functionsObserved,
                           ArrayList<DoublePoint> functionsTheoretical,
                           ArrayList<ArrayList<DoublePoint>> implementations) {
        JFrame window = new ScatterChart("means", meansObserved, meansTheoretical, new ArrayList<>());
        window.setVisible(true);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JFrame window2 = new ScatterChart("correlation", functionsObserved, functionsTheoretical, new ArrayList<>());
        window2.setVisible(true);
        window2.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JFrame window3 = new ScatterChart("implementations", implementations.get(0), implementations.get(1), implementations.get(2));
        window3.setVisible(true);
        window3.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    static ArrayList<DoublePoint> fromArray(Double[] array, int stepsCount, double stepWidth) {
        ArrayList<DoublePoint> list = new ArrayList<>(array.length);

        for (int i = 0; i < stepsCount; i++) {
            list.add(new DoublePoint(getT(i, stepWidth), array[i]));
        }

        return list;
    }
}
