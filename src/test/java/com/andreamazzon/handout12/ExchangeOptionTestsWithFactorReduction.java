package com.andreamazzon.handout12;


import java.text.DecimalFormat;

import com.andreamazzon.handout10.LIBORMarketModelConstructionWithAddedClone;
import com.andreamazzon.handout10.MyExchangeOption;
import com.andreamazzon.handout12.LIBORMarketModelConstructionWithFactorReduction.Dynamics;
import com.andreamazzon.handout12.LIBORMarketModelConstructionWithFactorReduction.Measure;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;

/**
 * In this class we print the value of an exchange option involving two LIBOR rates taken from a LIBOR market model,
 * when the correlation structure between the LIBORs is given by an exponential decay model with changing exponential
 * decay parameter. We do this both applying and withouth applying factor reduction.
 *
 * @author Andrea Mazzon
 *
 */
public class ExchangeOptionTestsWithFactorReduction {

	static final DecimalFormat FORMATTERREAL2 = new DecimalFormat("0.00");
	static final DecimalFormat FORMATTERREAL4 = new DecimalFormat(" 0.0000;-0.0000");

	public static void main(final String[] args) throws CalculationException {
		//parameters for the option

		final double maturityDateFirstLibor = 2.0;
		final double paymentDateFirstLibor = 2.5;

		final double maturityDateSecondLibor = 4.0;
		final double paymentDateSecondLibor = 4.5;
		final double notional = 100;

		final AbstractLIBORMonteCarloProduct exchangeOption = new MyExchangeOption(maturityDateFirstLibor, paymentDateFirstLibor,
				maturityDateSecondLibor, paymentDateSecondLibor);

		//parameters for the LIBOR market model simulation

		final int numberOfPaths = 50000;
		final double simulationTimeStep = 0.1;
		final double LIBORTimeStep = 0.5;
		final double LIBORRateTimeHorizon = 10;

		final double a = 0.2, b = 0.1, c = 0.15, d = 0.3; //volatility structure

		//fixing times for the forwards: the forwards corresponding to other fixing times will be interpolated
		final double[] fixingForGivenForwards = { 0.5, 1.0, 3.0, 4.0, 9.0 };
		final double[] forwardsForCurve = { 0.05, 0.05, 0.05, 0.05, 0.05 };

		final double initialCorrelationDecayParameter = 0.05;

		final int reducedNumberOfFactors = 5;

		//no factor reduction
		final LIBORModelMonteCarloSimulationModel myLiborMarketModel =
				LIBORMarketModelConstructionWithFactorReduction.createLIBORMarketModel(numberOfPaths,
						simulationTimeStep,
						LIBORTimeStep, LIBORRateTimeHorizon,
						fixingForGivenForwards, forwardsForCurve,
						initialCorrelationDecayParameter,
						Dynamics.LOGNORMAL,
						Measure.SPOT,
						a,b,c,d,
						20);

		//factor reduction
		final LIBORModelMonteCarloSimulationModel myLiborMarketModelReduced =
				LIBORMarketModelConstructionWithFactorReduction.createLIBORMarketModel(numberOfPaths,
						simulationTimeStep,
						LIBORTimeStep, LIBORRateTimeHorizon,
						fixingForGivenForwards, forwardsForCurve,
						initialCorrelationDecayParameter,
						Dynamics.LOGNORMAL,
						Measure.SPOT,
						a,b,c,d,
						reducedNumberOfFactors);

		//price for the model with no factor reduction
		double optionValue =  notional * exchangeOption.getValue(myLiborMarketModel);

		//price for the model with factor reduction
		double optionValueReduced =  notional * exchangeOption.getValue(myLiborMarketModelReduced);

		double currentCorrelationDecayParameter = initialCorrelationDecayParameter;
		//rho_{i,k}=e^(-alpha|T_i-T_k|
		final double differenceBetweenMaturities = maturityDateSecondLibor-maturityDateFirstLibor;
		double correlationBetweenTheTwoLIBORs =
				Math.exp(-currentCorrelationDecayParameter*differenceBetweenMaturities);

		//we do the for loop where the correlation decay parameter increases by 0.1 every time
		final double correlationStep = 0.05;

		System.out.println("Correlation decay parameter" + "\t" + "Correlation"+ "\t" + "Option value with all factors"
				+ "\t" + "Option value with factor reduction" + "\t" + "Relative difference");

		for (int correlationIndex = 1; correlationIndex <= 10; correlationIndex++) {

			System.out.println(FORMATTERREAL2.format(currentCorrelationDecayParameter) + "\t"
					+ "                         "	+ FORMATTERREAL2.format(correlationBetweenTheTwoLIBORs)
					+ "          "	+ FORMATTERREAL4.format(optionValue)
					+ "                         "	+ FORMATTERREAL4.format(optionValueReduced)
					+ "                                  "	+ FORMATTERREAL4.format(Math.abs(optionValue-optionValueReduced)/optionValue));

			//rho_i = rho_0 + i * 0.1
			currentCorrelationDecayParameter += correlationStep;

			correlationBetweenTheTwoLIBORs = Math.exp(-currentCorrelationDecayParameter*differenceBetweenMaturities);

			final LIBORModelMonteCarloSimulationModel newLiborMarketModel =
					LIBORMarketModelConstructionWithAddedClone.getCloneWithModifiedCorrelation(myLiborMarketModel,
							currentCorrelationDecayParameter);

			final LIBORModelMonteCarloSimulationModel newLiborMarketModelReduced =
					LIBORMarketModelConstructionWithAddedClone.getCloneWithModifiedCorrelation(myLiborMarketModelReduced,
							currentCorrelationDecayParameter);

			optionValue =  notional * exchangeOption.getValue(newLiborMarketModel);
			optionValueReduced =  notional * exchangeOption.getValue(newLiborMarketModelReduced);

		}

		//we print the last ones
		System.out.println(FORMATTERREAL2.format(currentCorrelationDecayParameter) + "\t"
				+ "                         "	+ FORMATTERREAL2.format(correlationBetweenTheTwoLIBORs)
				+ "          "	+ FORMATTERREAL4.format(optionValue)
				+ "                         "	+ FORMATTERREAL4.format(optionValueReduced)
				+ "                                  "	+ FORMATTERREAL4.format(optionValue-optionValueReduced/optionValue));
	}
}