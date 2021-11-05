package com.andreamazzon.handout1;

import java.text.DecimalFormat;

import net.finmath.exception.CalculationException;
import net.finmath.functions.AnalyticFormulas;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationModel;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloBlackScholesModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * This class has a main method that heuristically checks the relation by which
 * the value of the delta of a call option with Black-Scholes model is equal to
 * the valuation of a portfolio holding 1/S0 asset-or-nothing options of same
 * maturity, with the same strike, of course written on the same underlying.
 * Question: does this relation hold in general or only for the Black-Scholes
 * model?
 *
 * @author Andrea Mazzon
 *
 */
public class AssetOrNothingCheck {

	static final DecimalFormat FORMATTERPOSITIVE4 = new DecimalFormat("0.0000");
	static final DecimalFormat FORMATTERPERCENTAGE2 = new DecimalFormat("0.00%");

	public static void main(String[] args) throws CalculationException {

		// model parameters
		final double initialPrice = 100.0;
		final double volatility = 0.25; // the volatility of the underlying
		final double riskFreeRate = 0;

		// option parameters
		final double strike = 100.0;
		final double maturity = 1.0;

		// simulation parameter
		final int numberOfSimulations = 1000000;// the number of paths simulated

		// time discretization parameters
		final double initialTime = 0;
		final int numberOfTimeSteps = 100;
		final double timeStep = maturity / numberOfTimeSteps;
		final TimeDiscretization times = new TimeDiscretizationFromArray(initialTime, numberOfTimeSteps, timeStep);

		// have a look at this class!
		final double analyticValueOfTheDelta = AnalyticFormulas.blackScholesOptionDelta(initialPrice, riskFreeRate,
				volatility, maturity, strike);

		/*
		 * look at the class: it links together the model, i.e., the specification of
		 * the dynamics of the underlying, and the process, i.e., the discretization of
		 * the paths.
		 */
		final AssetModelMonteCarloSimulationModel bsModel = new MonteCarloBlackScholesModel(times, numberOfSimulations,
				initialPrice, riskFreeRate, volatility);

		final AbstractAssetMonteCarloProduct assetOrNothingOption = new AssetOrNothing(maturity, strike);

		final double monteCarloValueOfDelta = assetOrNothingOption.getValue(bsModel) / initialPrice;

		final double absolutePercentageError = Math.abs(analyticValueOfTheDelta - monteCarloValueOfDelta)
				/ analyticValueOfTheDelta;

		System.out.println("B-S Monte Carlo value: " + FORMATTERPOSITIVE4.format(monteCarloValueOfDelta) + "\n"
				+ "Analytical value: " + FORMATTERPOSITIVE4.format(analyticValueOfTheDelta) + "\n"
				+ "Absolute percentage error: " + FORMATTERPERCENTAGE2.format(absolutePercentageError) + "\n");
	}

}
