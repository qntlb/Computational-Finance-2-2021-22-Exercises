package com.andreamazzon.tutorium;

import java.util.function.DoubleUnaryOperator;

import net.finmath.plots.Plot2D;

public class PlotExample {

	public static void main(String[] args) {

		final double xMin = 0.0;
		final double xMax = 2 * Math.PI;

		final int numberOfPoints = 100;

		DoubleUnaryOperator sinus = (x) -> Math.sin(x);

		Plot2D myPlot = new Plot2D(xMin, xMax, numberOfPoints, sinus);

		myPlot.setTitle("Plot of sin");
		myPlot.setXAxisLabel("x");
		myPlot.setYAxisLabel("Sinus");
		myPlot.show();

	}

}
