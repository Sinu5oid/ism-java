package com.aisystems.sinu5oid.stochastic;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ScatterChart extends JFrame {
    public ScatterChart(String title,
                        ArrayList<DoublePoint> points1,
                        ArrayList<DoublePoint> points2,
                        ArrayList<DoublePoint> points3) {
        super(title);

        this.points1 = points1;
        this.points2 = points2;
        this.points3 = points3;

        XYDataset dataset = createDataset();
        JFreeChart chart = ChartFactory.createXYLineChart(
                "",
                "X-Axis", "Y-Axis", dataset);

        ChartPanel panel = new ChartPanel(chart);
        final XYPlot plot = chart.getXYPlot();

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesPaint(1, Color.RED);
        renderer.setSeriesPaint(2, Color.BLACK);

        plot.setRenderer(renderer);

        setSize(1600, 900);
        setContentPane(panel);
    }

    private XYDataset createDataset() {
        final XYSeries ser1 = new XYSeries("1");
        for (DoublePoint point : points1) {
            ser1.add(point.getX(), point.getY());
        }

        final XYSeries ser2 = new XYSeries("2");
        for (DoublePoint point : points2) {
            ser2.add(point.getX(), point.getY());
        }

        final XYSeries ser3 = new XYSeries("3");
        for (DoublePoint point : points3) {
            ser3.add(point.getX(), point.getY());
        }

        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(ser1);
        dataset.addSeries(ser2);
        dataset.addSeries(ser3);
        return dataset;
    }

    private final ArrayList<DoublePoint> points1;
    private final ArrayList<DoublePoint> points2;
    private final ArrayList<DoublePoint> points3;
}