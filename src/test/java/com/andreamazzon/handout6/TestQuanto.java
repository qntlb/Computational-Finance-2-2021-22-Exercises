package com.andreamazzon.handout6;

import org.junit.Test;

/**
 * This is a test class for the price of a Quanto caplet
 *
 * @author Andrea Mazzon
 *
 */
public class TestQuanto {

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
	public void testQuanto() {
		// foreign Libor rate dynamics
		final double initialForwardForeignLibor = initialForwardLibor;
		final double liborForeignVolatility = liborVolatility;

		// forward fx rate dynamics
		final double fxVolatility = 0.2;

		// correlation between the forward fx rate process and the Libor rate process
		final double correlationFxLibor = 0.4;

		// the quanto rate (i.e., the the constant conversion factor)
		final double quantoRate = 0.9;

		final double quantoPrice = InterestRatesProductsFurtherEnhanced.calculateQuantoCapletValue(
				initialForwardForeignLibor, liborForeignVolatility, fxVolatility, correlationFxLibor, fixingDate,
				paymentDate, strike, discountFactorAtMaturity, notional, quantoRate);

		System.out.println("Price of the Quanto Caplet: " + quantoPrice);
	}

}
