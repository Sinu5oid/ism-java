package com.aisystems.sinu5oid.markov;

import org.jetbrains.annotations.NotNull;
import utils.MatrixUtils;

import java.util.Arrays;
import java.util.HashMap;

public class Engine {
    public Engine(double[][] transitionMatrix, int startingNode) throws InvalidTransitionMatrixException {
        validateMatrix(transitionMatrix, comparisonTolerance);

        this.transitionMatrix = transitionMatrix;
        this.startingNode = startingNode;
        this.stepsCount = transitionMatrix.length;
        this.cacheMap = new HashMap<>();
        this.comparisonTolerance = defaultComparisonTolerance;
    }

    public Engine(double[][] transitionMatrix, int startingNode, double comparisonTolerance) throws InvalidTransitionMatrixException {
        this(transitionMatrix, startingNode);

        this.comparisonTolerance = comparisonTolerance;
    }

    public void setStepsCount(int stepsCount) {
        this.stepsCount = stepsCount;
    }

    public double[] getTheoreticalProbabilityAt(int stepIndex) {
        String cacheKey = "tprob_" + stepIndex;

        double[] cached = cacheMap.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        if (stepIndex <= 0) {
            double[] res = new double[transitionMatrix.length];
            res[startingNode] = 1d;

            return res;
        }

        double[][] recursiveRow = new double[][]{getTheoreticalProbabilityAt(stepIndex - 1)};
        double[] res = MatrixUtils.multiplyMatrices(recursiveRow, transitionMatrix)[0];

        cacheMap.put(cacheKey, res);

        return res;
    }

    public double[] getEmpiricProbabilityAt(@NotNull int[][] implementations, int stepIndex) {
        return getEmpiricProbabilityAt(implementations, transitionMatrix.length, stepIndex);
    }

    public static double[] getEmpiricProbabilityAt(@NotNull int[][] implementations, int transitionMatrixLength, int stepIndex) {
        double[] res = new double[transitionMatrixLength];

        for (int[] implementation : implementations) {
            if (implementation.length <= stepIndex) {
                // safe check
                // (if steps count or implementation length has been changed)
                res[implementation[implementation.length - 1]]++;
                continue;
            }

            res[implementation[stepIndex]]++;
        }

        for (int i = 0; i < res.length; i++) {
            res[i] /= implementations.length;
        }

        return res;
    }

    public int[] generateImplementation(int stepsCount) {
        int[] res = new int[stepsCount + 1];
        int currentNode = startingNode;

        IStepGenerator[] generators = new IStepGenerator[transitionMatrix.length];
        for (int i = 0; i < transitionMatrix.length; i++) {
            generators[i] = getGenerator(transitionMatrix[i]);
        }

        // prefill with starting pos
        Arrays.fill(res, 0, stepsCount, currentNode);
        for (int i = 1; i < stepsCount + 1; i++) {
            double[] probRow = transitionMatrix[currentNode];

            if (Math.abs(probRow[currentNode] - 1) < comparisonTolerance) {
                Arrays.fill(res, i, stepsCount, currentNode);

                return res;
            }

            currentNode = generators[currentNode].next();
            res[i] = currentNode;
        }

        return res;
    }

    public int[] generateImplementation() {
        return generateImplementation(stepsCount);
    }

    protected static IStepGenerator getGenerator(@NotNull double[] probRow) {
        return new RandomGenerator(probRow);
    }

    private static void validateMatrix(double[][] transitionMatrix, double comparisonTolerance) throws InvalidTransitionMatrixException {
        if (transitionMatrix == null) {
            throw new InvalidTransitionMatrixException("transition matrix is null");
        }

        if (transitionMatrix.length == 0) {
            throw new InvalidTransitionMatrixException("transition matrix is empty");
        }

        int referenceRowLength = transitionMatrix[0].length;
        for (double[] row : transitionMatrix) {
            if (referenceRowLength != row.length) {
                throw new InvalidTransitionMatrixException("transition matrix is inconsistent");
            }

            double cumulativeProb = 0;

            for (double prob : row) {
                if (prob < 0) {
                    throw new InvalidTransitionMatrixException("transition matrix contains negative probability");
                }

                cumulativeProb += prob;
            }

            if (Math.abs(cumulativeProb - 1) > comparisonTolerance) {
                throw new InvalidTransitionMatrixException("transition matrix has row with probability sum too far");
            }
        }
    }

    static class InvalidTransitionMatrixException extends Exception {
        public InvalidTransitionMatrixException(String message) {
            super(message);
        }
    }

    public static final double defaultComparisonTolerance = 0.000001d;

    protected final double[][] transitionMatrix;
    protected final int startingNode;
    protected int stepsCount;

    private final HashMap<String, double[]> cacheMap;
    private double comparisonTolerance;
}
