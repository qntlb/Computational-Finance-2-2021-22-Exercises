package com.andreamazzon.handout4;

import org.junit.Assert;
import org.junit.Test;

import net.finmath.exception.CalculationException;
import net.finmath.functions.AnalyticFormulas;

/**
 * This is a test class, checking if the Caplet values computed by the two
 * overloaded methods Products.calculateCapletValueBlackModel are equal, up to a
 * given tolerance, to the one computed by
 * AnalyticFormulas.blackModelCapletValue in the finmath library.
 *
 * @author Andrea Mazzon
 */
public class CapletTest {

	// discount factor
	final double discountFactorAtMaturity = 0.91;

	// parameters for the Libor dynamics
	final double initialForwardLibor = 0.05;
	final double liborVolatility = 0.3;

	// parameters for the both the options
	final double fixingDate = 1;
	final double paymentDate = 2;

	final double strike = 0.044;
	final double notional = 10000;

	@Test
	public void capletTest() throws CalculationException {

		// tolerances for assertEqual
		final double toleranceForAnalytic = 1E-15;
		final double toleranceForMonteCarlo = 1E-2;

		// parameters for the Monte Carlo simulation
		final int numberOfTimeSteps = 10;
		final int numberOfSimulations = 100000;

		final double maturityOfTheOption = fixingDate;
		final double periodLength = paymentDate - fixingDate;

		// the benchmark value
		final double finmathLibraryValue = notional * AnalyticFormulas.blackModelCapletValue(initialForwardLibor,
				liborVolatility, maturityOfTheOption, strike, periodLength, discountFactorAtMaturity);

		System.out.println("Value of the caplet computed by the Finmath library: " + finmathLibraryValue + "\n");

		// the value computed using the analytic BS formula from the finmath library
		final double ourAnalyticValue = InterestRatesProducts.calculateCapletValueBlackModel(initialForwardLibor,
				liborVolatility, strike, fixingDate, paymentDate, discountFactorAtMaturity, notional);

		System.out.println("Value of the caplet computed using the analytic formula for a call option: "
				+ ourAnalyticValue + "\n");

		/*
		 * we divide by the notional because otherwise the test could fail if the
		 * notional is too big. Another way could be to look at the relative error
		 */
		Assert.assertEquals(finmathLibraryValue / notional, ourAnalyticValue / notional, toleranceForAnalytic);

		// the value computed using the Monte Carlo method
		final double ourMonteCarloValue = InterestRatesProducts.calculateCapletValueBlackModel(initialForwardLibor,
				liborVolatility, strike, fixingDate, paymentDate, discountFactorAtMaturity, notional, numberOfTimeSteps,
				numberOfSimulations);

		System.out.println("Value of the caplet computed using the Monte Carlo valuation of a call option: "
				+ ourMonteCarloValue + "\n");

		/*
		 * we divide by the notional because otherwise the test could fail if the
		 * notional is too big. Another way could be to look at the relative error
		 */
		Assert.assertEquals(finmathLibraryValue / notional, ourMonteCarloValue / notional, toleranceForMonteCarlo);

	}

}
