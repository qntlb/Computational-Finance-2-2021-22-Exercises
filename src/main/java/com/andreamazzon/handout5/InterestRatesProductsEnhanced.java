package com.andreamazzon.handout5;

import com.andreamazzon.handout3.Swap;
import com.andreamazzon.handout3.SwapWithoutFinmath;
import com.andreamazzon.handout4.InterestRatesProducts;

import net.finmath.exception.CalculationException;
import net.finmath.functions.AnalyticFormulas;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.BrownianMotionFromMersenneRandomNumbers;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationModel;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloMultiAssetBlackScholesModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * This class extends the class InterestRatesProducts providing the pricing of
 * two more methods: one to compute the price of the swaption when the
 * underlying is given by a Bachelier model, and one to compute the price of a
 * cap made by two caplets, when the underlyings can be correlated.
 *
 * @author Andrea Mazzon
 *
 */
public class InterestRatesProductsEnhanced extends InterestRatesProducts {

	/**
	 * Calculate the value of a swaption under the Bachelier model. The price of the
	 * swaption is computed as the price of a call option on the par swap rate S,
	 * times the annuity. For this reason, among other things we also need the
	 * initial value of the par swap rate, in order to give it to the method
	 * computing the value of the call option.
	 *
	 * @param bondCurve,          the zero coupon bond curve
	 * @param tenureStructure,    the TimeDiscretization representing the swap times
	 * @param strike,             the strike of the option: S_i=K for all i
	 * @param notional,           i.e. N
	 * @param swapRateVolatility, the volatility of the swap process (normal
	 *                            dynamics)
	 */
	public static double calculateSwaptionValueBachelier(double[] zeroBondCurve, TimeDiscretization tenureStructure,
			double strike, double notional, double swapRateVolatility) {
		final int curveLength = zeroBondCurve.length;
		// we need to compute annuity and par swap rate
		final Swap mySwap = new SwapWithoutFinmath(tenureStructure, zeroBondCurve, true);
		final double initialSwapRate = mySwap.getParSwapRate();
		final double annuity = (zeroBondCurve[0] - zeroBondCurve[curveLength - 1]) / initialSwapRate;
		final double exerciseDate = tenureStructure.getTime(1); // T_1
		/*
		 * note that you can also avoid to multiply by the annuity, and give annuity
		 * instead of 1 as payoffUnit in AnalyticFormulas.bachelierOptionValue
		 */
		return notional * AnalyticFormulas.bachelierOptionValue(initialSwapRate, swapRateVolatility, exerciseDate,
				strike, annuity);

	}

	/**
	 * Calculate the value of a swaption under the Bachelier model when the payment
	 * dates are evenly distributed, i.e., when the time step of the tenure
	 * structure is constant.
	 *
	 * @param bondCurve,      the zero coupon bond curve
	 * @param yearFraction,   the length of the intervals between payment dates
	 * @param swapVolatility, the volatility of the swap process (normal dynamics)
	 * @param strike,         the strike of the option: S_i=K for all i
	 * @param notional,       i.e. N
	 */
	public static double calculateSwaptionValueBachelier(double[] zeroBondCurve, double yearFraction, double strike,
			double notional, double swapRateVolatility) {
		final int curveLength = zeroBondCurve.length;
		// Note the overloaded constructor with yearFraction
		final Swap mySwap = new SwapWithoutFinmath(yearFraction, zeroBondCurve, true);
		// overloaded method: we save time
		final double initialSwapRate = mySwap.getParSwapRate(yearFraction);
		final double annuity = (zeroBondCurve[0] - zeroBondCurve[curveLength - 1]) / initialSwapRate;
		final double exerciseDate = yearFraction; // T_1
		/*
		 * note that you can also avoid to multiply by the annuity, and give annuity
		 * instead of 1 as payoffUnit in AnalyticFormulas.bachelierOptionValue
		 */
		return notional * AnalyticFormulas.bachelierOptionValue(initialSwapRate, swapRateVolatility, exerciseDate,
				strike, annuity);

	}

	/**
	 * This method calculates and return the value of a cap involving two caplets
	 * under the Black model for the two Libors involved, using a Monte Carlo
	 * method.
	 *
	 * @param initialForwardLibor,                i.e. L^1_0 = L(T_1,T_2;0)
	 * @param initialForwardLibor,                i.e. L^2_0 = L(T_2,T_3;0)
	 * @param firstLiborVolatility,               the volatility of the first LIBOR
	 *                                            process under the Black model
	 * @param secondLiborVolatility               the volatility of the second LIBOR
	 *                                            process under the Black model
	 * @param correlation,                        the correlation between the two
	 *                                            Libors
	 * @param firstStrike,                        the strike of the first caplet
	 * @param secondStrike,                       the strike of the second caplet
	 * @param firstFixingDate,                    i.e. T_1
	 * @param secondFixingDate,                   i.e. T_2
	 * @param secondPaymentDate,                  i.e. T_3
	 * @param firstPaymentDateDiscountFactor,     i.e. P(T_2;0)
	 * @param secondPaymentDateDiscountFactor,    i.e. P(T_3;0)
	 * @param notional
	 * @param numberOfTimeStepsForDiscretization, the number of steps for the time
	 *                                            discretization we want to fix
	 * @param numberOfSimulations,                the number of simulations we want
	 *                                            to use for the Monte-Carlo
	 *                                            approximation of the price
	 * @throws CalculationException
	 */
	public static double calculateCapValueBlackModel(double initialFirstLibor, double initialSecondLibor,
			double firstLiborVolatility, double secondLiborVolatility, double correlation, double firstStrike,
			double secondStrike, double firstFixingDate, double secondFixingDate, double secondPaymentDate,
			double firstPaymentDateDiscountFactor, double secondPaymentDateDiscountFactor, double notional,
			int numberOfTimeStepsForDiscretization, int numberOfSimulations) throws CalculationException {

		// we first get the size of the time steps of the time discretization
		final double timeStep = secondFixingDate / numberOfTimeStepsForDiscretization;

		// we the create the time discretization
		final TimeDiscretization times = new TimeDiscretizationFromArray(0.0, numberOfTimeStepsForDiscretization,
				timeStep);

		/*
		 * And we create a two-dimensional Brownian motion: note that the components
		 * here are independent!
		 */
		final BrownianMotion twoDimBrownianMotion = new BrownianMotionFromMersenneRandomNumbers(times, 2,
				numberOfSimulations, 1897);// (B^1,B^2), independent

		/*
		 * this is the correlation matrix we want to have for the dependent Brownian
		 * motions: 1.0 represents of course the correlation of W^1 with itself (look at
		 * the handout) and of W^2 with itself. Instead, rho is the correlation of W^1
		 * and W^2: so, in the end we have W^1 = B^1, W^2 = rho B^1 + sqrt(1-rho^2) B_2
		 */
		final double[][] correlationMatrix = { { 1.0, correlation }, { correlation, 1.0 } };

		double[] initialLibors = { initialFirstLibor, initialSecondLibor };
		double[] volatilities = { firstLiborVolatility, secondLiborVolatility };

		/*
		 * Look at this class of the Finmath library: it permits you to simulate n
		 * processes, possibly correlated, all following log-normal dynamics.
		 */
		final AssetModelMonteCarloSimulationModel simulationTwoDimGeometricBrownian = new MonteCarloMultiAssetBlackScholesModel(
				twoDimBrownianMotion, initialLibors, 0, volatilities, correlationMatrix);

		final double firstPeriodLength = secondFixingDate - firstFixingDate;
		final double secondPeriodLength = secondPaymentDate - secondFixingDate;

		// the constants we give to SumOfCallOptions
		final double firstMultiplier = firstPaymentDateDiscountFactor * firstPeriodLength;
		final double secondMultiplier = secondPaymentDateDiscountFactor * secondPeriodLength;

		AbstractAssetMonteCarloProduct sumOfCallOptionsCalculator = new SumOfCallOptions(firstFixingDate,
				secondFixingDate, firstStrike, secondStrike, firstMultiplier, secondMultiplier);

		return notional * sumOfCallOptionsCalculator.getValue(simulationTwoDimGeometricBrownian);
	}
}
