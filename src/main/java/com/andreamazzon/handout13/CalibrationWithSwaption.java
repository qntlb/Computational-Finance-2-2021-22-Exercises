package com.andreamazzon.handout13;


import org.jblas.util.Random;

import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.marketdata.products.Swap;
import net.finmath.montecarlo.RandomVariableFactory;
import net.finmath.montecarlo.RandomVariableFromArrayFactory;
import net.finmath.montecarlo.interestrate.CalibrationProduct;
import net.finmath.montecarlo.interestrate.LIBORMarketModel;
import net.finmath.montecarlo.interestrate.LIBORModel;
import net.finmath.montecarlo.interestrate.LIBORMonteCarloSimulationFromLIBORModel;
import net.finmath.montecarlo.interestrate.models.LIBORMarketModelFromCovarianceModel;
import net.finmath.montecarlo.interestrate.models.covariance.AbstractLIBORCovarianceModelParametric;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCovarianceModelExponentialForm5Param;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;
import net.finmath.montecarlo.interestrate.products.Swaption;
import net.finmath.time.TimeDiscretization;

/**
 * This class performs a calibration of the LIBOR Market Model with respect to the covariance
 * structure. In particular, we assume an exponentially decaying correlation coupled with a
 * Rebonato volatility, and we want to calibrate its parameters. The calibration is based on prices
 * of swaption for different strikes.
 *
 * @author Andrea Mazzon
 */
public class CalibrationWithSwaption {

	//this will be filled with the products that we construct in order to calibrate
	CalibrationProduct[] calibrationProducts;

	/*
	 * This is the LIBOR Market model that we suppose (in our exercise) to give the prices that
	 * we observe: basically, we want that our calibration returns something close to this
	 */
	LIBORMonteCarloSimulationFromLIBORModel trueLiborMarket;
	LIBORModel trueLiborMarketModel;

	/**
	 * It creates an object used to calibrate the covariance structure of the LIBOR Market model
	 * based on prices of swaptions with different strikes.
	 *
	 * @param trueLiborMarket, it gives the underlying LIBOR Market model we want to calibrate: it
	 * provides the prices that we observe
	 */
	public CalibrationWithSwaption(LIBORMonteCarloSimulationFromLIBORModel trueLiborMarket) {
		this.trueLiborMarket = trueLiborMarket;
		//the model which is linked to the Brownian motion to construct the simulation
		trueLiborMarketModel =  trueLiborMarket.getModel();
	}

	/**
	 * This method creates a list of CalibrationProducts, and stores them in the
	 * field of the class: such a field is accessed by the swaptionCalibration()
	 * method
	 *
	 * @param numberOfStrikes: the number of strikes for which we observe the prices
	 *                         of swaptions: it is the number of prices we observe.
	 * @return the array of CalibrationProducts we construct
	 * @throws CalculationException
	 */
	public void createCalibrationItems(int numberOfStrikes) throws CalculationException {

		//we initialize the array
		calibrationProducts = new CalibrationProduct[numberOfStrikes];

		/*
		 * This is used:
		 * - to calculate the par swap rate (we need it to center the strikes on it)
		 * - to construct the swaptions on which we base our calibration
		 */
		final TimeDiscretization tenureStructure = trueLiborMarket.getLiborPeriodDiscretization();


		//here we use directly the method of the Finmath library
		final double swapRate = Swap.getForwardSwapRate(
				tenureStructure,
				// discretization for fixed and floating leg, in principle they are different!
				tenureStructure,
				// we get forwards and bonds from the model
				trueLiborMarketModel.getForwardRateCurve(), trueLiborMarketModel.getDiscountCurve());

		// our choice: how far out and in the money we want the strikes to be
		final double minStrike = 3.0/5.0*swapRate;
		final double maxStrike = 5.0/3.0*swapRate;
		final double strikeStep = (maxStrike - minStrike)/numberOfStrikes;

		/*
		 * Maybe we don't observe the exact prices that the model would give, but we
		 * have some noise (in addition to the Monte-Carlo error)
		 */
		double noise;

		// first time of the simulation
		final double fixingTime = tenureStructure.getTime(1);
		int counter = 0;// to place the prices in the array
		for (double strike = minStrike; strike <= maxStrike; strike += strikeStep) {


			final AbstractLIBORMonteCarloProduct monteCarloSwaption = new Swaption(
					fixingTime, tenureStructure, strike);

			// remember: Random.nextDouble() gives a number uniformly distributed in (0,1)
			noise = 0.05*(-0.5+Random.nextDouble());

			/*
			 * This is the value of the swaption we suppose to observe in the market: in our
			 * case, we produce it by Monte-Carlo
			 */
			final double targetValueSwaption = monteCarloSwaption.getValue(trueLiborMarket)*(1 + noise);

			/*
			 * Look at this constructor of CalibrationProduct, where the important inputs
			 * for now are the first two: the product we are considering (so type of
			 * pruduct, tenure structure, strike ecc) and the value we observe for this
			 * product. The calibration will be made with the scope to match these values as
			 * much as possible.
			 */

			calibrationProducts[counter] =
					new CalibrationProduct(monteCarloSwaption,
							targetValueSwaption,
							1.0 /* calibration weight*/);
			counter ++;
		}
	}

	/**
	 * This method performs the calibration of the LIBOR Market model to the
	 * swaption prices.
	 *
	 * @return the calibrated LIBOR market model
	 */
	public LIBORMarketModel swaptionCalibration() throws CalculationException {

		/*
		 * We have to construct the LIBOR Market model we want to calibrate: what we
		 * want to calibrate are the five parameters identifying the covariance
		 * structure, but of course we have to specify all the rest. We start wit the
		 * covariance structure
		 */
		final TimeDiscretization timeDiscretization = trueLiborMarket.getTimeDiscretization();
		final TimeDiscretization liborDiscretization = trueLiborMarket.
				getLiborPeriodDiscretization();
		final int numberOfFactors = trueLiborMarket.getNumberOfFactors();

		// Here we create the covariance model whose parameters we want to calibrate
		final AbstractLIBORCovarianceModelParametric covarianceModelParametric = new
				LIBORCovarianceModelExponentialForm5Param(
						timeDiscretization, liborDiscretization,
						numberOfFactors);

		// again things we need for the LIBOR Market model
		final ForwardCurve forwardCurve = trueLiborMarketModel.getForwardRateCurve();
		final DiscountCurve discountCurve = trueLiborMarketModel.getDiscountCurve();

		final RandomVariableFactory randomVariableFactory = new RandomVariableFromArrayFactory();

		// Calibrated LIBOR Market Model
		final LIBORMarketModel liborMarketModelCalibrated = new LIBORMarketModelFromCovarianceModel(
				liborDiscretization,
				null,
				forwardCurve,
				discountCurve,
				randomVariableFactory,
				//The parameters will be in fact computed by calibration
				covarianceModelParametric, calibrationProducts,
				null);

		//with the calibrated parameters for the covariance structure
		return liborMarketModelCalibrated;
	}

	/**
	 * It returns the calibration products
	 * 
	 * @return the array of the calibration products
	 */
	public CalibrationProduct[] getCalibrationProducts() {
		return calibrationProducts;
	}
}
