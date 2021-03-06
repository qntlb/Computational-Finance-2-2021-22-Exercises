package com.andreamazzon.recap;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.BrownianMotionFromMersenneRandomNumbers;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 *
 * This class gives an overview of the simulation of the Brownian Motion with
 * the Finmath library. In general, it has the goal to recap some fundamental
 * interfaces of the Finmath library, like TimeDiscretization and
 * RandomVariable, that constitute indeed the building blocks for the simulation
 * of stochastic processes.
 *
 * We want to approximate and print the evolution of:
 *
 * - the av. and the var. of a Brownian motion, generated by the Finmath library
 * - the av. and the var. of the quadratic variation of the 1st Brownian motion
 * - the av. and the var. of the quadratic covariation of two independent B.M.s
 *
 *
 * @author: Andrea Mazzon
 *
 */

public class BrownianMotionExperiments {

	/*
	 * Note how to set some printing settings. We use objects of type DecimalFormat,
	 * which is a class that extends NumberFormat (so that we can give to such an
	 * object a reference to NumberFormat). At the right, you see the constructor:
	 * we can give as many zeros after "." as many decimal digits we want.
	 */
	static final NumberFormat FORMATTERPOSITIVE2 = new DecimalFormat("0.00");
	static final NumberFormat FORMATTERREAL4 = new DecimalFormat(" 0.0000;-0.0000");

	public static void main(final String[] args) {

		// The parameters for the TimeDiscretization object
		final double firstTime = 0.0;
		final double lastTime = 1.0;
		final double dt = 0.01;// the time step

		// Time discretization: the number of steps is deduced from lastTime and dt
		final TimeDiscretization timeDiscretization = new TimeDiscretizationFromArray(firstTime, // first time of the
																									// time
																									// discretization
				(int) (lastTime / dt), // number of times
				dt // time step
		);

		// see how we can use methods of TimeDiscretization
		final int numberOfTimes = timeDiscretization.getNumberOfTimes();

		/*
		 * Note: these are arrays of RandomVariable objects. Indeed, for any fixed time
		 * in the time discretization, the value of the process is represented by a
		 * random variable object.
		 */
		final RandomVariable[] firstBrownianMotionPath = new RandomVariable[numberOfTimes];
		final RandomVariable[] secondBrownianMotionPath = new RandomVariable[numberOfTimes];

		final RandomVariable[] firstQuadraticVariationPath = new RandomVariable[numberOfTimes];

		final RandomVariable[] quadraticCovariationPath = new RandomVariable[numberOfTimes];

		// We set B_0 = 0. Note that this is a deterministic constant
		firstBrownianMotionPath[0] = new RandomVariableFromDoubleArray(0.0 /* the time */, 0.0 /* the value */);
		secondBrownianMotionPath[0] = new RandomVariableFromDoubleArray(0.0 /* the time */, 0.0 /* the value */);
		firstQuadraticVariationPath[0] = new RandomVariableFromDoubleArray(0.0 /* the time */, 0.0 /* the value */);
		quadraticCovariationPath[0] = new RandomVariableFromDoubleArray(0.0 /* the time */, 0.0 /* the value */);

		// the parameters for the BrownianMotion object
		final int numberOfPaths = 100000;// i.e., the number of simulated trajectories
		final int seed = 1897;// this is the seed that we need to generate the random numbers

		// We generate a 2-dimensional Brownian motion
		final BrownianMotion myBrownianMotion = new BrownianMotionFromMersenneRandomNumbers(timeDiscretization, // the
																												// time
																												// discretization
																												// of
																												// the
																												// BM
				2, // number of ind. Brownian motions that we generate: this is a 2-dim. B.M.
				numberOfPaths, // number of simulated paths
				seed // the seed that is needed to generate the Mersenne random numbers
		);

		/*
		 * Here we create two objects of type RandomVariable, that during the for loop
		 * running in time will store all the increments one by one. This is better than
		 * creating a new RandomVariable (i.e., a new object) every time.
		 */
		RandomVariable firstBrownianIncrement;
		RandomVariable secondBrownianIncrement;

		System.out.println("Average, variance and other properties of a BrownianMotion." + "\n Time step size (dt): "
				+ dt + "  Number of path: " + numberOfPaths + "\n");
		System.out.println("      " + "\t" + "  int dW_1 " + "\t" + "int dW_1 dW_1" + "\t" + "int dW_1 dW_2" + "\t");
		System.out.println("time" + "\t" + " mean" + "\t" + " var" + "\t" + " mean" + "\t" + " var" + "\t" + " mean"
				+ "\t" + " var");

		/*
		 * For every iteration of the for loop, we want to get and print the average and
		 * the variance of the samples of our two Brownian motions, of the quadratic
		 * variation of the first one and of the quadratic covariation of the two
		 * Brownian motions. Here some methods of BrownianMotion and RandomVariable come
		 * into play.
		 */
		for (int timeIndex = 1; timeIndex < numberOfTimes; timeIndex++) {

			/*
			 * We fix the increments: in this way we don't have to call the method every
			 * time we need them. Note the getBrownianIncrement method of BrownianMotion
			 */
			firstBrownianIncrement = myBrownianMotion.getBrownianIncrement(timeIndex - 1, 0);
			secondBrownianIncrement = myBrownianMotion.getBrownianIncrement(timeIndex - 1, 1);

			// We get W(t+dt) from dW(t)

			// first path
			firstBrownianMotionPath[timeIndex] = firstBrownianMotionPath[timeIndex - 1].add(firstBrownianIncrement);// B_(t_i)=B_(t_(i-1))+(B_(t_(i))-B_(t_(i-1)))
			// second path
			secondBrownianMotionPath[timeIndex] = secondBrownianMotionPath[timeIndex - 1].add(secondBrownianIncrement);

			// We compute the quadratic variation of the first path at the current time
			firstQuadraticVariationPath[timeIndex] = firstQuadraticVariationPath[timeIndex - 1]
					.add(firstBrownianIncrement.squared());

			// We compute the quadratic covariation of the two paths at the current time
			quadraticCovariationPath[timeIndex] = quadraticCovariationPath[timeIndex - 1]
					.add(firstBrownianIncrement.mult(secondBrownianIncrement));

			/*
			 * We compute and immediately print the average and the variance of the
			 * RandomVariable objects that we got above. In order to do that, we use the
			 * getAverage() and getVariance() methods of the interface RandomVariable. And
			 * in order to print them, we use the format method of the class DecimalFormat.
			 */
			System.out.println(FORMATTERPOSITIVE2.format(timeDiscretization.getTime(timeIndex)) + "\t"
					+ FORMATTERREAL4.format(firstBrownianMotionPath[timeIndex].getAverage()) + "\t"
					+ FORMATTERREAL4.format(firstBrownianMotionPath[timeIndex].getVariance()) + "\t"
					+ FORMATTERREAL4.format(firstQuadraticVariationPath[timeIndex].getAverage()) + "\t"
					+ FORMATTERREAL4.format(firstQuadraticVariationPath[timeIndex].getVariance()) + "\t"
					+ FORMATTERREAL4.format(quadraticCovariationPath[timeIndex].getAverage()) + "\t"
					+ FORMATTERREAL4.format(quadraticCovariationPath[timeIndex].getVariance()) + "\t" + "");
		}
		System.out.println("\n");

		/*
		 * One can also just pick a given time and analyse statistic by statistic, since
		 * we stored everything in an array.
		 */
		final double time = 0.5;
		// we want to get the time index corresponding to the time t=0.5
		final int indexForTheGivenTime = timeDiscretization.getTimeIndexNearestGreaterOrEqual(time);

		/*
		 * In order to get the mean and variance of the Brownian motion at the given
		 * time, we just look at the entry of the array at the time index corresponding
		 * to that time
		 */
		System.out.println("Mean of the first Brownian Motion at time " + time + " : "
				+ FORMATTERREAL4.format(firstBrownianMotionPath[indexForTheGivenTime].getAverage()));
		System.out.println("Variance of the first Brownian Motion at time " + time + " : "
				+ FORMATTERREAL4.format(firstBrownianMotionPath[indexForTheGivenTime].getVariance()) + "\n");

		// same thing for the quadratic variation..
		System.out.println("Mean of the Quadratic Variation of the first Brownian motion at time " + time + " : "
				+ FORMATTERREAL4.format(firstQuadraticVariationPath[indexForTheGivenTime].getAverage()));
		System.out.println("Variance of the Quadratic Variation of the first Brownian motion at time " + time + " : "
				+ FORMATTERREAL4.format(firstQuadraticVariationPath[indexForTheGivenTime].getVariance()) + "\n");

		// ..and for the covariation
		System.out.println("Mean of the Quadratic Covariation of the two Brownian motions at time " + time + " : "
				+ FORMATTERREAL4.format(quadraticCovariationPath[indexForTheGivenTime].getAverage()));
		System.out.println("Variance of the Quadratic Covariation of the two Brownian motions at time " + time + " : "
				+ FORMATTERREAL4.format(quadraticCovariationPath[indexForTheGivenTime].getVariance()));
	}
}