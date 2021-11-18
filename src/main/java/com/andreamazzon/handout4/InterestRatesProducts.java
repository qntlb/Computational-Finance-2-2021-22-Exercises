package com.andreamazzon.handout4;

import com.andreamazzon.handout3.Swap;
import com.andreamazzon.handout3.SwapWithoutFinmath;

import net.finmath.exception.CalculationException;
import net.finmath.functions.AnalyticFormulas;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationModel;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloBlackScholesModel;
import net.finmath.montecarlo.assetderivativevaluation.products.EuropeanOption;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * This class has methods computing the value of a caplet and of a swaption.
 *
 * @auhor: Andrea Mazzon
 */

public class InterestRatesProducts {

	/**
	 * This method calculates and returns the value of a Caplet under the Black
	 * model.
	 *
	 * @param initialForwardLibor,       i.e. L_0 = L(T_1,T_2;0)
	 * @param liborVolatility,           the volatility of the LIBOR process under
	 *                                   the Black model
	 * @param strike,                    the strike of the option
	 * @param fixing,                    i.e. T_1
	 * @param paymentDate,               i.e. T_2
	 * @param paymentDateDiscountFactor, i.e. P(T_2;0)
	 * @param notional,                  i.e. N
	 */
	public static double calculateCapletValueBlackModel(double initialForwardLibor, double liborVolatility,
			double strike, double fixingDate, double paymentDate, double paymentDateDiscountFactor, double notional) {
		final double periodLength = paymentDate - fixingDate;
		/*
		 * the discount factor is the bond maturing at the payment date; the LIBOR rate
		 * has no drift because we are changing to a certain equivalent measure under
		 * which it exhibits martingale dynamics.
		 */
		return notional * paymentDateDiscountFactor * periodLength
				* AnalyticFormulas.blackScholesOptionValue(initialForwardLibor, 0, liborVolatility, fixingDate, strike);
	}

	/**
	 * This method calculates and return the value of a Caplet under the Black
	 * model, using a Monte Carlo method.
	 *
	 * @param initialForwardLibor,       i.e. L_0 = L(T_1,T_2;0)
	 * @param liborVolatility,           the volatility of the LIBOR process under
	 *                                   the Black model
	 * @param strike,                    the strike of the option
	 * @param fixing,                    i.e. T_1
	 * @param paymentDate,               i.e. T_2
	 * @param paymentDateDiscountFactor, i.e. P(T_2;0)
	 * @param notional,                  i.e. N
	 * @throws CalculationException
	 */
	public static double calculateCapletValueBlackModel(double initialForwardLibor, double liborVolatility,
			double strike, double fixingDate, double paymentDate, double paymentDateDiscountFactor, double notional,
			int numberOfTimeStepsForDiscretization, int numberOfSimulations) throws CalculationException {

		// we first get the size of the time steps of the time discretization
		final double timeStep = fixingDate / numberOfTimeStepsForDiscretization;

		final TimeDiscretization times = new TimeDiscretizationFromArray(0.0, numberOfTimeStepsForDiscretization,
				timeStep);

		// we construct the simulation..
		final AssetModelMonteCarloSimulationModel blackModel = new MonteCarloBlackScholesModel(times,
				numberOfSimulations, initialForwardLibor, 0, liborVolatility);

		// ..and the object for the european option
		final EuropeanOption europeanOption = new EuropeanOption(fixingDate, strike);

		final double periodLength = paymentDate - fixingDate;
		/*
		 * the discount factor is the bond maturing at the payment date; the LIBOR rate
		 * has no drift because we are changing to a certain equivalent measure under
		 * which it exhibits martingale dynamics.
		 */
		return notional * paymentDateDiscountFactor * periodLength * europeanOption.getValue(blackModel);
	}

	/**
	 * It calculates the value of a swaption under the Black model. The price of the
	 * swaption is computed as the price of a call option on the par swap rate S,
	 * times the annuity. For this reason, among other things we also need the
	 * initial value of the par swap rate, in order to give it to the method
	 * computing the value of the call option.
	 *
	 * @param bondCurve,          the zero coupon bond curve
	 * @param tenureStructure,    the payment dates, given as a TimeDiscretization.
	 * @param swapRateVolatility, the volatility of the stochastic process modelling
	 *                            the evolution of the par swap rate (which has
	 *                            log-normal dynamics under the Black model)
	 * @param strike,             the strike of the option: the swaption pays
	 *                            max(V_swap(T1),0) where V_swap is the value of a
	 *                            swap with swap rates S_i=K for all i
	 * @param notional,           i.e. N
	 */
	public static double calculateSwaptionValueBlack(double[] zeroBondCurve, TimeDiscretization tenureStructure,
			double strike, double notional, double swapRateVolatility) {
		final int curveLength = zeroBondCurve.length;
		// we need this to compute annuity and par swap rate
		final Swap mySwap = new SwapWithoutFinmath(tenureStructure, zeroBondCurve, true);
		final double initialSwapRate = mySwap.getParSwapRate();
		// we know that initialSwapRate = (zeroBondCurve[0] - zeroBondCurve[curveLength
		// - 1]) / annuity
		final double annuity = (zeroBondCurve[0] - zeroBondCurve[curveLength - 1]) / initialSwapRate;
		final double exerciseDate = tenureStructure.getTime(1); // T_1

		return notional * annuity * AnalyticFormulas.blackScholesOptionValue(initialSwapRate, 0, swapRateVolatility,
				exerciseDate, strike);
	}

	/**
	 * Calculate the value of a swaption under the Black model when the payment
	 * dates are evenly distributed, i.e., when the time step of the tenure
	 * structure is constant.
	 *
	 * @param bondCurve,          the zero coupon bond curve
	 * @param tenureStructure,    the payment dates, given as a TimeDiscretization.
	 * @param swapRateVolatility, the volatility of the swap process (log-normal
	 *                            dynamics)
	 * @param strike,             the strike of the option: S_i=K for all i
	 * @param notional,           i.e. N
	 */
	public static double calculateSwaptionValueBlack(double[] zeroBondCurve, double yearFraction, double strike,
			double notional, double swapRateVolatility) {
		final int curveLength = zeroBondCurve.length;
		// we need id to compute annuity and par swap rate. Note the overloaded
		// constructor with yearFraction
		final Swap mySwap = new SwapWithoutFinmath(yearFraction, zeroBondCurve, true);
		// overloaded method: we save time
		final double initialSwapRate = mySwap.getParSwapRate(yearFraction);
		final double annuity = (zeroBondCurve[0] - zeroBondCurve[curveLength - 1]) / initialSwapRate;
		final double exerciseDate = yearFraction; // T_1
		return notional * annuity * AnalyticFormulas.blackScholesOptionValue(initialSwapRate, 0, swapRateVolatility,
				exerciseDate, strike);

	}

}
