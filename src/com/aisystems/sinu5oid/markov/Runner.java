package com.aisystems.sinu5oid.markov;

import java.util.logging.Logger;

public class Runner {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger(Runner.class.getName());

        double[][] transitionMatrix = {
            {1, 0, 0, 0, 0},
            {7.0 / 17.0, 6.0 / 17.0, 0, 4.0 / 17.0, 0},
            {0, 0, 0, 10.0 / 13.0, 3.0 / 13.0},
            {0, 0, 1.0 / 2.0, 1.0 / 2.0, 0},
            {8.0 / 15.0, 0, 0, 7.0 / 15.0, 0},
        };

        int startingNode = 2;
        int stepsCount = 25;
        int implementationCount = 10000000;

        Engine engine;
        try {
            logger.info("creating engine");
            engine = new Engine(transitionMatrix, startingNode);
            engine.setStepsCount(stepsCount);
            logger.info("engine created");

            logger.info("generating implementations");
            int[][] implementations = new int[implementationCount][];
            for (int i = 0; i < implementationCount; i++) {
                implementations[i] = engine.generateImplementation();
            }
            logger.info("implementations generated");

            logger.info("collecting probabilities");
            double[][] theoreticalProbabilities = new double[stepsCount + 1][];
            double[][] empiricProbabilities = new double[stepsCount + 1][];
            for (int i = 0; i < stepsCount + 1; i++) {
                theoreticalProbabilities[i] = engine.getTheoreticalProbabilityAt(i - 1);
                empiricProbabilities[i] = engine.getEmpiricProbabilityAt(implementations, transitionMatrix.length, i);
            }
            logger.info("probabilities collected");

            logger.info("comparison");
            for (int stepIdx = 0; stepIdx < stepsCount + 1; stepIdx++) {
                System.out.printf("step #%d\n", stepIdx);
                for (int probIdx = 0; probIdx < transitionMatrix.length; probIdx++) {
                    System.out.printf("t:\t%.6f\t|\te:\t%.6f\t(%+.6f)\n",
                            theoreticalProbabilities[stepIdx][probIdx],
                            empiricProbabilities[stepIdx][probIdx],
                            empiricProbabilities[stepIdx][probIdx] - theoreticalProbabilities[stepIdx][probIdx]);
                }
            }

        } catch (Engine.InvalidTransitionMatrixException e) {
            logger.severe("transition matrix is invalid: " + e.getMessage());
        }

    }
}
