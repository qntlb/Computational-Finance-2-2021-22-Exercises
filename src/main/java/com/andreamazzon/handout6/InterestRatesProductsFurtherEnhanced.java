package com.andreamazzon.handout6;

import com.andreamazzon.handout5.InterestRatesProductsEnhanced;

import net.finmath.functions.AnalyticFormulas;

/**
 * This class extends InterestRatesProductsEnhanced (and so indirectly also
 * InterestRatesProducts) adding the computation of the price of a Quanto
 * Caplet. The dynamics of both the foreign forward rate and the forward fx are
 * supposed to be log-normal.
 *
 * @author Andrea Mazzon
 *
 */
public class InterestRatesProductsFurtherEnhanced extends InterestRatesProductsEnhanced {
	/**
	 * This method calculates and returns the value of a Quanto Caplet, supposing
	 * log-normal dynamics for both the foreign forward rate and the forward fx rate
	 *
	 * @param initialForeignForwardLibor, the foreign forward LIBOR evaluated at
	 *                                    time 0
	 * @param liborForeignVolatility,     the volatility of the foreign forward
	 *                                    LIBOR process
	 * @param fxVolatility,               the volatility of the forward FX rate
	 *                                    process
	 * @param correlationFxLibor,         the correlation between the foreign LIBOR
	 *                                    process and the forward FX rate process
	 * @param fixing,                     i.e. T_1
	 * @param paymentDate,                i.e. T_2
	 * @param strike,                     the strike of the option
	 * @param paymentDateDiscountFactor,  i.e. P(T_2;0)
	 * @param notional,                   i.e. N
	 * @param quantoRate,                 the constant conversion factor
	 */
	public static double calculateQuantoCapletValue(double initialForeignForwardLibor, double foreignLiborVolatility,
			double fxVolatility, double correlationFxForeignLibor, double fixingDate, double paymentDate, double strike,
			double paymentDateDiscountFactor, double notionalInForeignCurrency, double quantoRate) {
		final double periodLength = paymentDate - fixingDate;

		/*
		 * Under the pricing measure, the foreign LIBOR has a drift which is given by
		 * -correlationFxLibor * liborForeignVolatility * fxVolatility. This determines
		 * the exponential term which multiplies initialForeignForwardLibor inside the
		 * Black-Scholes formula: see page 206 of the script.
		 */
		return notionalInForeignCurrency * quantoRate * paymentDateDiscountFactor * periodLength
				* AnalyticFormulas.blackScholesOptionValue(initialForeignForwardLibor
						* Math.exp(-correlationFxForeignLibor * foreignLiborVolatility * fxVolatility * fixingDate), 0,
						// - correlationFxForeignLibor * foreignLiborVolatility * fxVolatility,
						foreignLiborVolatility, fixingDate, strike);
	}

}
