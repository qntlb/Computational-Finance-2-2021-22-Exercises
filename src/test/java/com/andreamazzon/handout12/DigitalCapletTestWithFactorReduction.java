package com.andreamazzon.handout12;


import java.text.DecimalFormat;

import org.junit.Assert;
import org.junit.Test;

import com.andreamazzon.handout12.LIBORMarketModelConstructionWithFactorReduction.Dynamics;
import com.andreamazzon.handout12.LIBORMarketModelConstructionWithFactorReduction.Measure;
import com.andreamazzon.handout9.MyDigitalCaplet;

import net.finmath.functions.AnalyticFormulas;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.montecarlo.interestrate.LIBORMarketModel;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.TermStructureModel;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * This class tests the difference between analytic and
 *
 * @author andreamazzon
 *
 */
public class DigitalCapletTestWithFactorReduction {

	private final static DecimalFormat formatterDouble = new DecimalFormat("0.00");

	private final static DecimalFormat formatterDeviation = new DecimalFormat("0.000%;");


	@Test
	public void testDigitalCaplet() throws Exception {


		final int numberOfPaths = 100000;//the number of simulated processes

		final double simulationTimeStep = 0.1;//for the SIMULATION discretization
		//of course the two time discretizations are not required to be the same!
		final double liborPeriodLength = 0.5;//for the TENURE STRUCTURE discretization
		final double liborRateTimeHorizon = 10;

		final double correlationDecayParam = 30;//alpha such that rho_{i,j}=\exp(-alpha|T_i-T_j|)
		final double a = 0.2, b = 0.1, c = 0.15, d = 0.3; //volatility structure
		/*
		 * The fixing (or maturity) dates for which the initial values of the forwards/Libors are given.
		 * For example, in our case we have the value of L(0.5,1;0), L(1,1.5;0),L(3,3.5;0), L(4,4.5;0),
		 * L(9.5,10;0)
		 */
		final double[] fixingForForwards = { 0.5, 1.0, 3.0, 4.0, liborRateTimeHorizon - liborPeriodLength};
		//times for the forwards: the others will be interpolated (in our case, this is simple :) )
		final double[] forwardsForCurve = { 0.05, 0.05, 0.05, 0.05, 0.05 };

		//final int numberOfFactors = (int) (liborRateTimeHorizon/liborPeriodLength);
		final int numberOfFactors = 2;

		//we construct the simulation
		final LIBORModelMonteCarloSimulationModel myLiborMonteCarlo =
				LIBORMarketModelConstructionWithFactorReduction.createLIBORMarketModel(numberOfPaths,
						simulationTimeStep,
						liborPeriodLength, liborRateTimeHorizon,
						fixingForForwards, forwardsForCurve,
						correlationDecayParam /* Correlation */,
						Dynamics.LOGNORMAL,
						Measure.SPOT,
						a,b,c,d,
						numberOfFactors
						);


		//parameters for the digital caplet
		final double strike = 0.05;
		final double notional = 10000;

		//parameter for the comparison between Monte Carlo and analytical price
		final double tolerance = 1E-1;

		/*
		 * In order to get the analytical prices, we need the volatilities sigma_i(t_j), for any index i moving
		 * in the time discretization of the tenure structure and any t_j moving in the time discretization
		 * of the simulation. In order to do this, we can use the method getIntegratedLIBORCovariance() of
		 * LIBORMarketModel. It returns a three-dimensional matrix: its (i,j,k) element is the integrated covariance
		 * of the Libors L(T_j,T_{j+1}) and L(T_k,T_{k+1}), evaluated  at time t_i. We have to give it the time
		 * discretization for the simulated processes.
		 */
		final TimeDiscretization simulationTimeDiscretization = new TimeDiscretizationFromArray(0.0,
				(int) (liborRateTimeHorizon / simulationTimeStep), simulationTimeStep);

		final TermStructureModel liborModel = myLiborMonteCarlo.getModel();
		// getIntegratedLIBORCovariance() is defined in LIBORMarketModel: we need to downcast
		final double[][][] integratedVarianceMatrix = ((LIBORMarketModel) liborModel).
				getIntegratedLIBORCovariance(simulationTimeDiscretization);

		//extract the discount curve (i.e., the zero coupon bonds curve) in order to get the analytical price
		final DiscountCurve discountFactors = liborModel.getDiscountCurve();

		//extract the forward curve (i.e., the Libor curve) in order to get the analytical price
		final ForwardCurve forwards = liborModel.getForwardRateCurve();

		System.out.println("Digital caplet prices:\n");

		System.out.println("Maturity      Simulation       Analytic       Abs Difference");

		final long start = System.currentTimeMillis();

		double errorsSum = 0;
		for (int maturityIndex = 1; maturityIndex <= myLiborMonteCarlo.getNumberOfLibors()-1;
				maturityIndex ++) {

			final double optionMaturity = myLiborMonteCarlo.getLiborPeriod(maturityIndex);//T_i
			System.out.print(formatterDouble.format(optionMaturity ) + "          ");

			final double optionPaymentDate = myLiborMonteCarlo.getLiborPeriod(maturityIndex + 1);//T_{i+1}

			// Computation of the Monte Carlo value
			final MyDigitalCaplet digitalCaplet = new MyDigitalCaplet(
					optionMaturity, optionPaymentDate, strike);

			final double valueSimulation = notional * digitalCaplet.getValue(myLiborMonteCarlo);

			System.out.print(formatterDouble.format(valueSimulation) + "          ");

			//computation of the analytical value
			final double periodLength = optionPaymentDate - optionMaturity;


			// first we get of the volatility sigma_i(t_j). Here i = liborIndex, j = t_j = T_i.

			/*
			 * we need the index for the maturity also in the time discretization for the simulated processes,
			 * to get the element we want of the matrix for the integrated covariance.
			 */
			final int maturityIndexInTheSimulationDiscretization = myLiborMonteCarlo.getTimeIndex(optionMaturity);

			final double integratedVariance = integratedVarianceMatrix
					[maturityIndexInTheSimulationDiscretization][maturityIndex][maturityIndex];
			final double variance = integratedVariance/optionMaturity;
			final double standardDeviation = Math.sqrt(variance);

			final double forward = forwards.getForward(null, optionMaturity);//L(T_i,T_{i+1};0)
			final double discountFactor = discountFactors.getDiscountFactor(optionPaymentDate);//P(T_{i+1};0)

			final double valueAnalytic = notional*AnalyticFormulas.
					blackModelDgitialCapletValue( forward, standardDeviation, periodLength,
							discountFactor , optionMaturity , strike);

			final double relativeDifference = Math.abs(valueSimulation - valueAnalytic)/valueAnalytic;

			System.out.print(formatterDouble.format(valueAnalytic) + "        ");
			// Relative deviation
			System.out.println(formatterDeviation.format(relativeDifference) );
			Assert.assertTrue(relativeDifference < tolerance);

			errorsSum += relativeDifference;

		}
		double averageError = errorsSum / (myLiborMonteCarlo.getNumberOfLibors()-1);
		System.out.println();
		System.out.println("The average error relative is " + averageError);
		System.out.println();
		final long finish = System.currentTimeMillis();
		final long timeElapsed = finish - start;
		System.out.println("It took "+ timeElapsed + " milliseconds");
	}
}