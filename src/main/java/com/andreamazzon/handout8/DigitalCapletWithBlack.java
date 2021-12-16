package com.andreamazzon.handout8;

import net.finmath.functions.AnalyticFormulas;

/**
 * This class takes care of the computations of the classical price (i.e., for
 * payment date T_2) and of the price in arrears (i.e., for payment date T_1) of
 * a digital caplet under the Black model. Note that the computation of the
 * convexity adjustment and consequently of the price in arrears is implemented
 * in parent, abstract class EuropeanOptionPossiblyInArrears: these quantities
 * can be computed once the value of the classical price as a function of the
 * initial value of the Libor is known, see the derivation in "Solution to
 * exercise 1" in com.andreamazzon.handout7.
 *
 * @author Andrea Mazzon
 *
 */
public class DigitalCapletWithBlack extends EuropeanOptionPossiblyInArrears {

	private final double strike;// this is a field specific to the digital caplet

	public DigitalCapletWithBlack(double firstTime, double secondTime, double firstBondOrInitialLibor,
			double secondBond, double liborVolatility, double notional, Boolean giveLibor, double strike) {
		// parent constructor
		super(firstTime, secondTime, firstBondOrInitialLibor, secondBond, liborVolatility, notional, giveLibor);
		this.strike = strike;// initialization of the derived class specific field
	}

	@Override
	public double getValueInClassicUnits(double initialValueLibor) {
		// see page 169 of the script
		return getNotional() * getSecondBond() * getTimeInterval() * AnalyticFormulas
				.blackScholesDigitalOptionValue(initialValueLibor, 0, getLiborVolatility(), getFirstTime(), strike);
	}
}