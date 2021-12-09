package com.andreamazzon.handout7;

import com.andreamazzon.handout6.InterestRatesProductsFurtherEnhanced;

import net.finmath.functions.AnalyticFormulas;

public class InterestRatesProductsInArrears extends InterestRatesProductsFurtherEnhanced {
	/**
	 * It calculates the value of a Caplet payed in arrears under the Black model.
	 *
	 * @param initialForwardLibor,    i.e. L_0 = L(T_1,T_2;0)
	 * @param liborVolatility,        the volatility of the LIBOR process
	 * @param strike,                 the strike of the option
	 * @param fixing,                 i.e. T_1
	 * @param maturity,               i.e. T_2
	 * @param maturityDiscountFactor, i.e. P(0;T_2)
	 * @param notional,               i.e. N
	 */
	public static double calculateCapletInArrearsBlack(double initialForwardLibor, double liborVolatility,
			double strike, double fixingDate, double paymentDate, double paymentDateDiscountFactor, double notional) {
		final double periodLength = paymentDate - fixingDate;

		/*
		 * Formula for the pricing of a Caplet payed in arrears, see the pdf for its
		 * derivation
		 */
		return notional * paymentDateDiscountFactor * periodLength
				* AnalyticFormulas.blackScholesOptionValue(initialForwardLibor, 0, liborVolatility, fixingDate, strike) // classic
				+ notional * paymentDateDiscountFactor * periodLength * periodLength * initialForwardLibor
						* AnalyticFormulas.blackScholesOptionValue(
								initialForwardLibor * Math.exp(liborVolatility * liborVolatility * fixingDate), 0,
								liborVolatility, fixingDate, strike);
	}

}
