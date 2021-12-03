package com.andreamazzon.tutorium;

import java.util.function.DoubleUnaryOperator;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationModel;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloBlackScholesModel;
import net.finmath.plots.Plot2D;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

public class PlotExampleWithStochasticProcess {
	public static void main(String[] args) {

		final double initialTime = 0.0;
		final int numberOfTimeSteps = 1000;
		final double timeStep = 0.05;

		TimeDiscretization times = new TimeDiscretizationFromArray(initialTime, numberOfTimeSteps, timeStep);
		final int numberOfPaths = 2;

		final double initialValue = 1.0;
		final double volatility = 0.3;
		final double riskFreeRate = 0.0;

		AssetModelMonteCarloSimulationModel myBSSimulation = new MonteCarloBlackScholesModel(times, numberOfPaths,
				initialValue, riskFreeRate, volatility);

		final double initialTimeForPlot = initialTime;
		final double finalTimeForPlot = numberOfTimeSteps * timeStep;
		final int numberOfPointsWePlot = numberOfTimeSteps + 1;

		DoubleUnaryOperator bsSimulationFunction = (t) -> {
			final int simulationIndex = 0;

			double myRealization = 0.0;
			try {
				RandomVariable realizationAtTimeT = myBSSimulation.getAssetValue(t, simulationIndex);
				// double[] realizationsAsArray = realizationAtTimeT.getRealizations();
				// myRealization = realizationsAsArray[0];
				myRealization = realizationAtTimeT.get(0);
			} catch (CalculationException e) {
				e.printStackTrace();
			}

			return myRealization;
			/* realization of S_t(omega_0) for that time t */ };

		DoubleUnaryOperator bsSimulationFunctionSecond = (t) -> {
			final int simulationIndex = 0;

			double myRealization = 0.0;
			try {
				RandomVariable realizationAtTimeT = myBSSimulation.getAssetValue(t, simulationIndex);
				// double[] realizationsAsArray = realizationAtTimeT.getRealizations();
				// myRealization = realizationsAsArray[0];
				myRealization = realizationAtTimeT.get(1);
			} catch (CalculationException e) {
				e.printStackTrace();
			}

			return myRealization;
			/* realization of S_t(omega_0) for that time t */ };

		DoubleUnaryOperator[] bsSimulations = { bsSimulationFunction, bsSimulationFunctionSecond };
		Plot2D myPlot = new Plot2D(initialTimeForPlot, finalTimeForPlot, numberOfPointsWePlot, bsSimulations);

		myPlot.show();

	}
}
