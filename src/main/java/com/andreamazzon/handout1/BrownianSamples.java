package com.andreamazzon.handout1;

import java.text.DecimalFormat;
import java.util.Random;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.BrownianMotionFromMersenneRandomNumbers;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * This class has a main method whose goal is to compute some statistics about
 * the average of the value of a Brownian motion at a given point in time: we
 * use the implementation we have seen last time to simulate trajectories of a
 * Brownian motion for a given seed, and then we repeat the experiment changing
 * the seed randomly. All the averages that we get in this way form an array of
 * doubles. This array gets then wrapped into a RandomVariable object, in order
 * to compute average, variance, maximum and minimum using the methods
 * implemented in the Finmath library.
 *
 * @author Andrea Mazzon
 *
 */
public class BrownianSamples {
	static final DecimalFormat FORMATTERPOSITIVE4 = new DecimalFormat("0.0000");

	public static void main(String[] args) throws CalculationException {

		/*
		 * This is a Java class that we use here in order to get random integer numbers
		 * that will represent the seeds.
		 */
		final Random randomGenerator = new Random();
		final int numberOfAverage = 1000;

		/*
		 * It is supposed to contain all the averages for given seeds. It gets then
		 * wrapped into a RandomVariable.
		 */
		final double[] vectorOfAverages = new double[numberOfAverage];

		final double finalTime = 1.0;

		// simulation parameter
		final int numberOfSimulations = 1000;// the number of paths simulated every time

		// time discretization parameters
		final double initialTime = 0;
		final int numberOfTimeSteps = 100;
		final double timeStep = finalTime / numberOfTimeSteps;
		final TimeDiscretization times = new TimeDiscretizationFromArray(initialTime, numberOfTimeSteps, timeStep);

		final BrownianMotion brownianMotion = new BrownianMotionFromMersenneRandomNumbers(times, 1, numberOfSimulations, // number
																															// of
																															// simulated
																															// paths
				1897 // the seed that is needed to generate the Mersenne random numbers
		);

		RandomVariable brownianIncrement;
		/*
		 * This time we don't need to store the values of the Brownian motion at
		 * different times in an array, so we just have a RandomVariable object that
		 * will get updated
		 */
		RandomVariable brownianMotionCurrentValue = new RandomVariableFromDoubleArray(0.0 /* the time */,
				0.0 /* the value */);

		for (int timeIndex = 1; timeIndex < numberOfTimeSteps + 1; timeIndex++) {

			brownianIncrement = brownianMotion.getBrownianIncrement(timeIndex - 1, 0);

			// B_(t_i)=B_(t_(i-1))+(B_(t_(i))-B_(t_(i-1)))
			brownianMotionCurrentValue = brownianMotionCurrentValue.add(brownianIncrement);
		}

		final double average = brownianMotionCurrentValue.getAverage();

		// first entry of the array: the average of this sample of simulations
		vectorOfAverages[0] = average;
		BrownianMotion brownianMotionWithModifiedSeed;

		/*
		 * Now we get all the averages for all the random seeds. We store them in the
		 * array
		 */
		int seed;// it's better to create it once for all here, outside the for loop.

		for (int i = 1; i < numberOfAverage; i++) {
			seed = randomGenerator.nextInt();// random int
			/*
			 * Note here the getCloneWithModifiedSeed method: we don't have to bother
			 * constructing the object from scratch as before
			 */
			brownianMotionWithModifiedSeed = brownianMotion.getCloneWithModifiedSeed(seed);

			RandomVariable brownianMotionWithModifiedSeedCurrentValue = new RandomVariableFromDoubleArray(
					0.0 /* the time */, 0.0 /* the value */);
			;
			for (int timeIndex = 1; timeIndex < numberOfTimeSteps + 1; timeIndex++) {

				brownianIncrement = brownianMotionWithModifiedSeed.getBrownianIncrement(timeIndex - 1, 0);
				// B_(t_i)=B_(t_(i-1))+(B_(t_(i))-B_(t_(i-1)))
				brownianMotionWithModifiedSeedCurrentValue = brownianMotionWithModifiedSeedCurrentValue
						.add(brownianIncrement);

			}
			vectorOfAverages[i] = brownianMotionWithModifiedSeedCurrentValue.getAverage();// we store the price
		}

		/*
		 * Now we wrap the array into one object of type RandomVariable. There are
		 * multiple ways to do this, here you can see two
		 */
		final RandomVariable priceRandomVariable = new RandomVariableFromDoubleArray(0.0, vectorOfAverages);
		// or:
		// final RandomVariable priceRandomVariable = (new
		// RandomVariableFromArrayFactory()).createRandomVariable(0.0, vectorOfPrices);

		System.out.println("Average= " + priceRandomVariable.getAverage());
		System.out.println("Variance= " + priceRandomVariable.getVariance());
		System.out.println("Min= " + priceRandomVariable.getMin());
		System.out.println("Max= " + priceRandomVariable.getMax());
	}

}