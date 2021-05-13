package utils;

public class MatrixUtils {
    public static double[][] multiplyMatrices(double[][] a, double[][] b) {
        if (a == null) {
            throw new NullPointerException("'a' matrix is null");
        }

        if (b == null) {
            throw new NullPointerException("'b' matrix is null");
        }

        if (a.length == 0 || b.length == 0 || a[0].length != b.length) {
            throw new IllegalArgumentException("matrices could not be multiplied");
        }

        double[][] res = new double[a.length][];
        for (int i = 0; i < a.length; i++) {
            res[i] = new double[b[0].length];
            for (int j = 0; j < b[0].length; j++) {
                for (int k = 0; k < b.length; k++) {
                    res[i][j] += a[i][k] * b[k][j];
                }
            }
        }

        return res;
    }
}
