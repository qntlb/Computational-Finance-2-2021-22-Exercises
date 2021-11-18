package com.andreamazzon.handout4;

import java.text.DecimalFormat;

import org.junit.Test;

import com.andreamazzon.handout3.Swap;
import com.andreamazzon.handout3.SwapWithoutFinmath;

import net.finmath.functions.AnalyticFormulas;

/**
 * This is a test class with a method getting and printing the price of a
 * swaption under the Black model.
 *
 * @author Andrea Mazzon
 *
 */
public class SwaptionTest {

	@Test
	public void testSwaption() {
		final DecimalFormat FORMATTERREAL2 = new DecimalFormat("0.00");
		final DecimalFormat FORMATTERREAL4 = new DecimalFormat("0.0000");

		// parameters for the swaption
		final double[] bondCurve = { 0.98, 0.95, 0.92, 0.9, 0.87 };
		final double yearFraction = 1;
		final double swapRateVolatility = 0.3;
		final double swaptionStrike = 0.03;
		final double notional = 10000;

		final double swaptionValue = InterestRatesProducts.calculateSwaptionValueBlack(bondCurve, yearFraction,
				swaptionStrike, notional, swapRateVolatility);

		System.out.println("Swaption value: " + FORMATTERREAL4.format(swaptionValue));
		System.out.println();

		/*
		 * now we want to see how the price of the swaption reacts if the value of the
		 * second bond P(T_2;0) increases
		 */

		double newValueForSecondBond = 0.93; // we start from 0.93: it must be bigger than P(T_3;0)

		while (newValueForSecondBond < 0.98) {// it must be smaller than P(T_1;0)
			bondCurve[1] = newValueForSecondBond;
			// new price of the swaption for the new value of P(T_2;0)
			final double newSwaptionValueForDifferentSecondBond = InterestRatesProducts
					.calculateSwaptionValueBlack(bondCurve, yearFraction, swaptionStrike, notional, swapRateVolatility);

			System.out.println("P(T_2;0)= " + FORMATTERREAL2.format(newValueForSecondBond) + " swaption value: "
					+ FORMATTERREAL4.format(newSwaptionValueForDifferentSecondBond));
			newValueForSecondBond += 0.01;// it increases by 0.01 at every iteration
		}
		System.out.println();

		// Let's investigate a bit more..

		/*
		 * We have that our price, as a function of the initial value S_0 of the par
		 * swap rate, is (P(T_1;0)-P(T_n;0))/S_0 BS(S_0,..) and S_0 is decreasing with
		 * respect to P(T_2;0) (look at the formula, or how it is computed in the method
		 * of SwapWithoutFinmath). So we want to look at the behavior of the function
		 * 1/x BS(x,..) with respect to x. In particular for this function we have that
		 * (1/x BS(x,..))'= 1/x DeltaBS(x,..) - 1/x^2BS(x,..) = 1/x(DeltaBS(x,..) - 1/x
		 * BS(x,..)). We then have to check if DeltaBS(x,..) > 1/x BS(x,..) for our
		 * values. In this case, the price of the swaption is increasing with respect to
		 * S_0 and then decreasing with respect to P(T_2;0).
		 */

		final Swap mySwap = new SwapWithoutFinmath(yearFraction, bondCurve, true);
		// we compute the par swap rate at 0
		final double initialSwapRate = mySwap.getParSwapRate(yearFraction);

		// price of the call option for our parameters, and our value of S_0
		final double valueOfTheCall = AnalyticFormulas.blackScholesOptionValue(initialSwapRate, 0.0, swapRateVolatility,
				yearFraction, swaptionStrike);

		final double valueOfTheCallDividedByInitial = valueOfTheCall / initialSwapRate;

		// delta of the call option for our parameters, and our value of S_0
		final double deltaOfTheCall = AnalyticFormulas.blackScholesOptionDelta(initialSwapRate, 0.0, swapRateVolatility,
				yearFraction, swaptionStrike);

		// let's now investigate the behavior with respect to the value of P(T_1;0).
		System.out.println("Call divided by initial value: " + FORMATTERREAL2.format(valueOfTheCallDividedByInitial)
				+ ". Delta of the call: " + FORMATTERREAL2.format(deltaOfTheCall));
	}

}
