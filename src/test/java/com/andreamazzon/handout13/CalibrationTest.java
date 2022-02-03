package com.andreamazzon.handout13;

import java.text.DecimalFormat;

import org.junit.Test;

import com.andreamazzon.handout12.LIBORMarketModelConstructionWithFactorReduction;
import com.andreamazzon.handout12.LIBORMarketModelConstructionWithFactorReduction.Dynamics;
import com.andreamazzon.handout12.LIBORMarketModelConstructionWithFactorReduction.Measure;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.BrownianMotionFromMersenneRandomNumbers;
import net.finmath.montecarlo.interestrate.CalibrationProduct;
import net.finmath.montecarlo.interestrate.LIBORMarketModel;
import net.finmath.montecarlo.interestrate.LIBORMonteCarloSimulationFromLIBORModel;
import net.finmath.montecarlo.interestrate.models.covariance.AbstractLIBORCovarianceModelParametric;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.montecarlo.process.MonteCarloProcess;

/**
 * In this class we test the calibration of the LIBOR Market model to swaptions
 * for different strikes
 *
 * @author Andrea Mazzon
 *
 */
public class CalibrationTest {

	static final DecimalFormat FORMATTERREAL2 = new DecimalFormat("0.00");
	static final DecimalFormat FORMATTERREAL4 = new DecimalFormat(" 0.0000;-0.0000");
	static final DecimalFormat FORMATTERREALPERCENTAGE = new DecimalFormat(" 0.00%;");


	@Test
	public void testCalibration() throws CalculationException {


		System.out.println("Calibration to Swaptions:");
		System.out.println();
		final int numberOfPaths = 10000;//the number of simulated processes

		final double simulationTimeStep = 0.1;//for the SIMULATION discretization
		final double liborPeriodLength = 0.5;//for the TENURE STRUCTURE discretization
		final double liborRateTimeHorizon = 10;

		/*
		 * These are the parameters of the LLIBOR Market model that we use to calibrate:
		 * will we get something similar after the calibration?
		 */
		final double correlationDecayParam = 0.3;//alpha such that rho_{i,j}=\exp(-alpha|T_i-T_j|)
		final double a = 0.5, b = 0.7, c = 0.35, d = 0.1; //volatility structure
		/*
		 * The fixing (or maturity) dates for which the initial values of the forwards/Libors are given.
		 * For example, in our case we have the value of L(0.5,1;0), L(1,1.5;0),L(3,3.5;0), L(4,4.5;0),
		 * L(9.5,10;0)
		 */
		final double[] fixingForForwards = { 0.5, 1.0, 3.0, 4.0, liborRateTimeHorizon - liborPeriodLength};
		//times for the forwards: the others will be interpolated (in our case, this is simple :) )
		final double[] forwardsForCurve = { 0.05, 0.05, 0.05, 0.05, 0.05 };

		final int numberOfFactors = 5;

		/*
		 * First step: construction of the calibration products: we have to create a
		 * LIBOR market model and to pass it as a parameter to
		 * Calibration.createCalibrationItems
		 */
		final LIBORMonteCarloSimulationFromLIBORModel libor = (LIBORMonteCarloSimulationFromLIBORModel) LIBORMarketModelConstructionWithFactorReduction.createLIBORMarketModel(numberOfPaths,
				simulationTimeStep,
				liborPeriodLength, liborRateTimeHorizon,
				fixingForForwards, forwardsForCurve,
				correlationDecayParam /* Correlation */,
				Dynamics.LOGNORMAL,
				Measure.SPOT,
				a,b,c,d,
				numberOfFactors
				);

		final CalibrationWithSwaption calibration = new CalibrationWithSwaption(libor);

		final int numberOfStrikesForTheCalibration = 15;
		calibration.createCalibrationItems(numberOfStrikesForTheCalibration);

		// second step: we calibrate using the constructed LIBOR market model
		final LIBORMarketModel liborMarketModelCalibrated = calibration.swaptionCalibration();

		/*
		 * We get the calibrated parameters by downcasting the covariance of the
		 * calibrated model to the parametric type, and use the getter
		 */
		final double[] parameters = ((AbstractLIBORCovarianceModelParametric)
				liborMarketModelCalibrated.getCovarianceModel()).getParameterAsDouble();
		/*
		 * getter in the abstractParametric class (downcasting, implemented in the 5pAram
		 * covariance model
		 */
		System.out.println("Rebonato volatility:");
		System.out.println("a = " + FORMATTERREAL2.format(parameters[0]));
		System.out.println("b = " + FORMATTERREAL2.format(parameters[1]));
		System.out.println("c = " + FORMATTERREAL2.format(parameters[2]));
		System.out.println("d = " + FORMATTERREAL2.format(parameters[3]));

		System.out.println();

		System.out.println("Covariance structure: decay parameter = " +
				FORMATTERREAL2.format(parameters[4]));
		System.out.println();
		/*
		 * third step: we construct the calibrated simulation, linking together the
		 * (calibrated) model and the Euler scheme as usual
		 */
		final BrownianMotion brownianMotion = new BrownianMotionFromMersenneRandomNumbers(
				libor.getTimeDiscretization(),
				numberOfFactors,//here we maybe perform factor reduction
				numberOfPaths,
				1897 // seed
				);

		final MonteCarloProcess process = new
				EulerSchemeFromProcessModel(liborMarketModelCalibrated, brownianMotion);

		final LIBORMonteCarloSimulationFromLIBORModel calibratedModelSimulator = new
				LIBORMonteCarloSimulationFromLIBORModel(process);


		final CalibrationProduct[] calibrationProducts = calibration.getCalibrationProducts();

		AbstractLIBORMonteCarloProduct derivative;

		System.out.println(" Model" + "\t" + "            Target" + "\t" + "  Percentage Error");

		System.out.println();
		for (final CalibrationProduct calibrationProduct : calibrationProducts) {

			// getter of the i-th calibration product: a Swaption object with a specific
			// strike
			derivative = calibrationProduct.getProduct();
			/*
			 * usual getValue method of an object of type AbstractLIBORMonteCarloProduct: it
			 * gives the Black volatility of the Swaption for the calibrated parameters
			 */
			final double valueModel = derivative.getValue(calibratedModelSimulator);
			// other getter from the arrayList: here we get the target volatility
			final double valueTarget = derivative.getValue(libor);
			// calibration error
			final double diff = Math.abs(valueModel - valueTarget)/valueTarget;
			System.out.println(FORMATTERREAL4.format(valueModel) + "\t \t   " +
					FORMATTERREAL4.format(valueTarget) + "\t "
					+ FORMATTERREALPERCENTAGE.format(diff));
		}
	}
}
