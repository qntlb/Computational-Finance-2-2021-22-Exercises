package com.andreamazzon.tutorium;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.BrownianMotionFromMersenneRandomNumbers;
import net.finmath.montecarlo.IndependentIncrements;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationModel;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloAssetModel;
import net.finmath.montecarlo.assetderivativevaluation.models.BachelierModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.montecarlo.assetderivativevaluation.products.DigitalOption;
import net.finmath.montecarlo.model.ProcessModel;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * We here construct an object of type AssetModelMonteCarloSimulationModel
 * representing a Bachelier simulation to compute the Monte-Carlo price of a
 * digital option.
 *
 * @author Andrea Mazzon
 *
 */
public class ExampleCallWithBachelier {

	public static void main(String[] args) throws CalculationException {

		// the model: what we want to simulate
		final double initialValue = 100;
		final double volatility = 0.5;
		final double interestRate = 0.1;

		ProcessModel bachelierModel = new BachelierModel(initialValue, interestRate, volatility);

		/*
		 * And then the Brownian motion: how we want to simulate the process: time
		 * discretization, number of simulations, seed
		 */

		// first we construct the TimeDiscretization object
		final double initialTime = 0.0;
		final double timeStep = 0.1;
		final int numberOfTimeSteps = 10;

		TimeDiscretization times = new TimeDiscretizationFromArray(initialTime, numberOfTimeSteps, timeStep);

		// then we give number of simulations and seed
		final int numberOfFactors = 1;
		final int numberOfPaths = 100000;
		final int seed = 1897;

		IndependentIncrements brownianMotion = new BrownianMotionFromMersenneRandomNumbers(times, numberOfFactors,
				numberOfPaths, seed);

		/*
		 * So we link model and Brownian Motion to create an object of type
		 * AssetModelMonteCarloSimulationModel
		 */
		AssetModelMonteCarloSimulationModel bachelierSimulation = new MonteCarloAssetModel(bachelierModel,
				brownianMotion);

		// and then we construct the option..
		final double maturity = 1.0;
		final double strike = 100;

		AbstractAssetMonteCarloProduct digitalOption = new DigitalOption(maturity, strike);

		// ..and get and print the price
		double price = digitalOption.getValue(bachelierSimulation);

		System.out.println("The price is " + price);

	}

}
