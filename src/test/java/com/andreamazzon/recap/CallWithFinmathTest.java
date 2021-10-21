package com.andreamazzon.recap;

import java.text.DecimalFormat;

import org.junit.Assert;
import org.junit.Test;

import net.finmath.exception.CalculationException;
import net.finmath.functions.AnalyticFormulas;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationModel;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloBlackScholesModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.montecarlo.assetderivativevaluation.products.EuropeanOption;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * This is a small test class, just to have a look about tests with JUnit work.
 * It has a method that is basically the first part of the main method in
 * CallWithFinmath. Note the use of the method Assert.assertTrue.
 *
 * @author Andrea Mazzon
 *
 */
public class CallWithFinmathTest {

	static final DecimalFormat FORMATTERPOSITIVE4 = new DecimalFormat("0.0000");
	static final DecimalFormat FORMATTERPERCENTAGE = new DecimalFormat("0.00%");

	@Test
	public void test() throws CalculationException {
		// process parameters
		final double initialPrice = 100.0;
		final double volatility = 0.25; // the volatility of the underlying
		final double riskFreeRate = 0.1;

		// option parameters
		final double strike = 100.0;
		final double maturity = 1.0;

		// simulation parameter
		final int numberOfSimulations = 100000;// the number of paths simulated

		// time discretization parameters
		final double initialTime = 0;
		final int numberOfTimeSteps = 100;
		final double timeStep = maturity / numberOfTimeSteps;
		final TimeDiscretization times = new TimeDiscretizationFromArray(initialTime, numberOfTimeSteps, timeStep);

		// needed for the assertTrue method
		final double tolerance = 0.05;

		/*
		 * Look at the private constructor in MonteCarloBlackScholesModel, line 95: it
		 * links together the model, i.e., the specification of the dynamics of the
		 * underlying, and the process, i.e., the discretization of the paths.
		 */
		final AssetModelMonteCarloSimulationModel bsModel = new MonteCarloBlackScholesModel(times, numberOfSimulations,
				initialPrice, riskFreeRate, volatility);

		/*
		 * In order to compute the value of the call option, we use the class
		 * EuropeanOption. It (indirectly) implements the interface
		 * AssetMonteCarloProduct, and extends AbstractAssetMonteCarloProduct: I can
		 * give it a AbstractAssetMonteCarloProduct reference.
		 */
		final AbstractAssetMonteCarloProduct europeanOption = new EuropeanOption(maturity, strike);

		final double analyticValue = AnalyticFormulas.blackScholesOptionValue(initialPrice, riskFreeRate, volatility,
				maturity, strike);

		final double monteCarloValue = europeanOption.getValue(bsModel);

		final double absolutePercentageError = Math.abs(analyticValue - monteCarloValue) / analyticValue;

		System.out.println("B-S Monte Carlo value: " + FORMATTERPOSITIVE4.format(monteCarloValue) + "\n"
				+ "Analytical value: " + FORMATTERPOSITIVE4.format(analyticValue) + "\n" + "Absolute percentage error: "
				+ FORMATTERPERCENTAGE.format(absolutePercentageError) + "\n");

		/*
		 * We want to check if the boolean value surrounded by parenthesis is True: if
		 * not, the test fails.
		 */
		Assert.assertTrue(absolutePercentageError < tolerance);

		// Another way to check if two numbers are close to each-other is the following:

		// Assert.assertEquals(analyticValue, monteCarloValue, 0.2);
	}

}
