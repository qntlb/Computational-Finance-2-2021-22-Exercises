package com.andreamazzon.recap;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import net.finmath.exception.CalculationException;
import net.finmath.functions.AnalyticFormulas;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.BrownianMotionFromMersenneRandomNumbers;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationModel;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloBlackScholesModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.montecarlo.assetderivativevaluation.products.EuropeanOption;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * The main method of this class computes the value of a call option by Monte
 * Carlo using the classes of the Finmath library. It also computes the
 * analytical value and then the absolute error. It does the same using the same
 * model, but with a modified seed to generate the random numbers.
 *
 * Here we see:
 *
 * - how to link model and process to produce an object representing a
 * Monte-Carlo simulation (not directly here, but having a look at the
 * constructor(s) of the class MonteCarloBlackScholesModel of the Finmath lib)
 *
 * -how to construct an object of type MonteCarloBlackScholesModel, in two ways:
 * constructing a Brownian m. or only giving the time discretization parameters
 *
 * - how to pass such an object to an AbstractAssetMonteCarloProduct class in
 * order to evaluate an option: in this case, a call option (EuropeanOption)
 *
 * -how to get the same object with modified seed (simple) or modified data in
 * general (more complicated)
 *
 *
 * @author Andrea Mazzon
 *
 */
public class CallWithFinmath {

	static final DecimalFormat FORMATTERPOSITIVE4 = new DecimalFormat("0.0000");
	static final DecimalFormat FORMATTERPERCENTAGE = new DecimalFormat("0.00%");

	public static void main(String[] args) throws CalculationException {

		// option parameters
		final double strike = 100.0;
		final double maturity = 1.0;

		// model parameters
		final double initialValue = 100.0;
		final double volatility = 0.25; // the volatility of the underlying
		final double riskFreeRate = 0.1;

		// simulation parameter
		final int numberOfSimulations = 100000;// the number of paths simulated

		// time discretization parameters
		final double initialTime = 0;
		final int numberOfTimeSteps = 100;
		final double timeStep = maturity / numberOfTimeSteps;
		final TimeDiscretization times = new TimeDiscretizationFromArray(initialTime, numberOfTimeSteps, timeStep);

		/*
		 * Look at the private constructor in MonteCarloBlackScholesModel, line 95: it
		 * links together the model, i.e., the specification of the dynamics of the
		 * underlying, and the process, i.e., the discretization of the paths.
		 */
		final AssetModelMonteCarloSimulationModel bsModel = new MonteCarloBlackScholesModel(times, numberOfSimulations,
				initialValue, riskFreeRate, volatility);

		/*
		 * In order to compute the value of the call option, we use the class
		 * EuropeanOption. It (indirectly) implements the interface
		 * AssetMonteCarloProduct, and extends AbstractAssetMonteCarloProduct: I can
		 * give it a AbstractAssetMonteCarloProduct reference.
		 */
		final AbstractAssetMonteCarloProduct europeanOption = new EuropeanOption(maturity, strike);

		// have a look at this class!
		final double analyticValue = AnalyticFormulas.blackScholesOptionValue(initialValue, riskFreeRate, volatility,
				maturity, strike);

		/*
		 * Note the getValue method: where is getValue(MonteCarloSimulationModel model)
		 * implemented? Not in EuropeanOption, if you have a look..
		 */
		final double monteCarloValue = europeanOption.getValue(bsModel);

		System.out.println("B-S Monte Carlo value: " + FORMATTERPOSITIVE4.format(monteCarloValue) + "\n"
				+ "Analytical value: " + FORMATTERPOSITIVE4.format(analyticValue) + "\n" + "Absolute percentage error: "
				+ FORMATTERPERCENTAGE.format(Math.abs(analyticValue - monteCarloValue) / analyticValue) + "\n");

		// Now we want to do the very same thing, but with a new seed
		final int newSeed = 1897;

		final AssetModelMonteCarloSimulationModel bsModelWithModifiedDrift = bsModel.getCloneWithModifiedSeed(newSeed);

		final double newMonteCarloValue = europeanOption.getValue(bsModelWithModifiedDrift);

		System.out.println("New B-S Monte Carlo value: " + FORMATTERPOSITIVE4.format(newMonteCarloValue) + "\n"
				+ "New absolute percentage error: "
				+ FORMATTERPERCENTAGE.format(Math.abs(analyticValue - newMonteCarloValue) / analyticValue) + "\n");

		// another way to simulate the Black-Scholes model, by giving a Brownian motion

		// Generating a one-dimensional Brownian motion
		final BrownianMotion brownianMotionForBlackScholes = new BrownianMotionFromMersenneRandomNumbers(times, // the
																												// time
																												// discretization
																												// of
																												// the
																												// Brownian
																												// motion
				1, // number of independent Brownian motions that we generate
				numberOfSimulations, // number of simulated paths
				newSeed // the seed that is needed to generate the Mersenne random numbers
		);

		/*
		 * This is an overloaded constructor, with respect to the one we have seen
		 * above: it now takes the Brownian motion instead of the time discretization
		 * parameters
		 */
		final AssetModelMonteCarloSimulationModel bsModelWithBrownianMotion = new MonteCarloBlackScholesModel(
				initialValue, riskFreeRate, volatility, brownianMotionForBlackScholes);

		final double thirdMonteCarloValue = europeanOption.getValue(bsModelWithBrownianMotion);

		System.out.println("B-S Monte Carlo value using the Brownian motion: "
				+ FORMATTERPOSITIVE4.format(thirdMonteCarloValue) + "\n" + "Absolute percentage error: "
				+ FORMATTERPERCENTAGE.format(Math.abs(analyticValue - thirdMonteCarloValue) / analyticValue) + "\n");

		/*
		 * We want now to get the price of the option for the same model,when the price
		 * is 10 euros higher. In order to do this, we see now something a bit more
		 * advanced: we construct a map to be used in the method
		 * getCloneWithModifiedData of MonteCarloBlackScholesModel.
		 */
		final double howMuchMore = 10;
		/*
		 * Map is an interface to map keys (given by strings in our case) to values
		 * (doubles in this case). HashMap is a class implementing the interface.
		 */
		final Map<String, Object> modifiedInitialValue = new HashMap<String, Object>();

		/*
		 * Put is one of the most used methods of Map. It associates a value to a key:
		 * in this case, the key is "initialValue" and the value is
		 * "initialValue + howMuchMore".
		 */
		modifiedInitialValue.put("initialValue", initialValue + howMuchMore);

		final AssetModelMonteCarloSimulationModel bsModelWithHigherInitialValue = bsModelWithBrownianMotion
				.getCloneWithModifiedData(modifiedInitialValue);

		System.out.println("B-S Monte Carlo value with the modified initial value: "
				+ FORMATTERPOSITIVE4.format(europeanOption.getValue(bsModelWithHigherInitialValue)));
	}
}