package com.andreamazzon.handout5;

import java.text.DecimalFormat;

import org.junit.Test;

import net.finmath.exception.CalculationException;

/**
 * In this class we compute the price of a Cap involving three dates T_1, T_2
 * and T_3 together with two caplets, for a specific choice of the parameters.
 * The Libors are supposed to follow log-normal dynamics.
 *
 * @author Andrea Mazzon
 *
 */
public class CapTest {

	final DecimalFormat FORMATTERREAL2 = new DecimalFormat("0.00");
	final DecimalFormat FORMATTERREAL4 = new DecimalFormat("0.0000");

	@Test
	public void CapTwoCapletsTest() throws CalculationException {

		// discount factors
		final double firstDiscountFactor = 0.91;
		final double secondDiscountFactor = 0.82;

		// parameters for the Libor dynamics
		final double initialFirstLibor = 0.05;
		final double initialSecondLibor = 0.04;

		final double firstLiborVolatility = 0.3;
		final double secondLiborVolatility = 0.25;

		// parameters for the options: dates..
		final double firstFixingDate = 1; // T_1
		final double secondFixingDate = 1.5; // T_2: we could also call it firstPaymentDate
		final double secondPaymentDate = 2; // T_3

		// ..and strikes
		final double firstStrike = 0.05;
		final double secondStrike = 0.04;
		final double notional = 1000;

		// parameters for the Monte Carlo simulation
		final int numberOfTimeSteps = 20;
		final int numberOfSimulations = 100000;

		final double correlation = 0.2;

		final double capValue = InterestRatesProductsEnhanced.calculateCapValueBlackModel(initialFirstLibor,
				initialSecondLibor, firstLiborVolatility, secondLiborVolatility, correlation, firstStrike, secondStrike,
				firstFixingDate, secondFixingDate, secondPaymentDate, firstDiscountFactor, secondDiscountFactor,
				notional, numberOfTimeSteps, numberOfSimulations);

		System.out.println("The value of the cap is " + capValue);

		System.out.println();

		// now we investigate how the price changes with the correlation

		System.out.println("Correlation" + "\t" + "Cap price");

		System.out.println();

		double newCapValue;

		for (double runningCorrelation = -1; runningCorrelation <= 1; runningCorrelation += 0.1) {

			newCapValue = InterestRatesProductsEnhanced.calculateCapValueBlackModel(initialFirstLibor,
					initialSecondLibor, firstLiborVolatility, secondLiborVolatility, runningCorrelation, firstStrike,
					secondStrike, firstFixingDate, secondFixingDate, secondPaymentDate, firstDiscountFactor,
					secondDiscountFactor, notional, numberOfTimeSteps, numberOfSimulations);

			System.out
					.println(FORMATTERREAL2.format(runningCorrelation) + "\t \t" + FORMATTERREAL4.format(newCapValue));
		}

	}

}
