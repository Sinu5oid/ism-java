package com.aisystems.sinu5oid.stochastic;

public class DoublePoint {
    public DoublePoint(double x, double y) {
        X = x;
        Y = y;
    }

    public double getY() {
        return Y;
    }
    public double getX() {
        return X;
    }

    private final double X;
    private final double Y;
}