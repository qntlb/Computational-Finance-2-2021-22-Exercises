package com.andreamazzon.handout2;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.finmath.exception.CalculationException;

/**
 * This class bootstraps the zero coupon bond curve from the values of coupon
 * bonds: the idea is that you take the formula for the value of a coupon bond
 * and you use it -knowing the value of the associated coupons C_i- to determine
 * the value of one bond, also knowing the value of the other bonds. Here we
 * suppose that the time step of the tenure structure is constant. The class is
 * supposed to be used in the following way: first, one constructs an object
 * giving the value of the first coupon bond and of the first coupon. Then the
 * method nextBondFromCouponBond gets called iteratively, so that the
 * bootstrapped bonds are stored in a List and used again to compute the new
 * bonds. Then the bonds can be got by calling the method getBonds.
 *
 * @author: Andrea Mazzon
 */

public class Bootstrap {

	/*
	 * A list is basically a more flexible array: you don't have to set the length
	 * from the beginning, but you instead simply append an element after the other.
	 * Here we use it because we don't know how many bonds we want to get
	 */
	private final List<Double> computedBonds = new ArrayList<Double>();
	/*
	 * We use it in order to host the sum of the elements
	 * C_i(T_{i+1}-T_i)P(T_{i+1};0) from 1 to m-1. This will be used to get the
	 * value of P(T_{i+1};0). We want it to be updated every time we get a new bond
	 */
	private double sumOfProductTimeStepBondsAndCoupons;

	private final double yearFraction;// the constant value T_{i+1}-T_i

	/**
	 * It constructs a new object to iteratively compute the value of zero coupon
	 * bonds from the values of coupon bonds and coupons.
	 *
	 * @param yearFraction,         T_2-T_1
	 * @param valueFirstCoupon,     C_1
	 * @param valueFirstCouponBond, C_1(T_2-T_1)P(T_2;0) + P(T_2;0)
	 */
	public Bootstrap(double yearFraction, double valueFirstCoupon, double valueFirstCouponBond) {

		Double valueFirstBond = valueFirstCouponBond / (1 + yearFraction * valueFirstCoupon);
		computedBonds.add(valueFirstBond);// we append the first element to our list
		// the sum is initialized. Note: the first bond is not included!
		sumOfProductTimeStepBondsAndCoupons = valueFirstBond * valueFirstCoupon * yearFraction;
		this.yearFraction = yearFraction;
	}

	/**
	 * Computes a new bond from the previously computed ones and from the new coupon
	 * bond. Internally, it also add the new bond to the bond list and updates the
	 * sum
	 *
	 * @param parSwapRate, the par swap rate for the given period
	 */
	public void nextBondFromCouponBond(double valueNewCouponBond, double valueNewCoupon) {
		Double valueNewBond = (valueNewCouponBond - sumOfProductTimeStepBondsAndCoupons)
				/ (1 + yearFraction * valueNewCoupon);
		sumOfProductTimeStepBondsAndCoupons += valueNewBond * valueNewCoupon * yearFraction;// note: the sum is updated!
		computedBonds.add(valueNewBond);
	}

	/**
	 * It returns all the bonds bootstrapped (at the moment when the method is
	 * called) from the coupon bonds
	 *
	 * @return
	 */
	public List<Double> getBonds() {
		return computedBonds;
	}

	public static void main(String[] args) throws CalculationException {
		final DecimalFormat FORMATTERREAL4 = new DecimalFormat("0.0000");

		// these are the vales of the coupon bonds we will give, one by one
		final double[] couponBonds = { 1.93, 2.77, 3.55, 4.45, 5.2, 5.9, 6.55, 7.15 };
		// these are the vales of the coupons we will give, one by one
		final double[] coupons = { 2.1, 1.9, 1.8, 2.2, 2.1, 1.95, 2, 2.05 };
		final double yearFraction = 0.5;// the constant T_{i+1}-T_i

		final double curveLength = couponBonds.length;
		final Bootstrap bootstrap = new Bootstrap(yearFraction, coupons[0], couponBonds[0]);

		/*
		 * Now we call the nextBondFromCouponBond iteratively, to bootstrap the bonds
		 * and use them to compute the next ones
		 */
		for (int couponBondIndex = 1; couponBondIndex < curveLength; couponBondIndex++) {
			bootstrap.nextBondFromCouponBond(couponBonds[couponBondIndex], coupons[couponBondIndex]);
		}

		final List<Double> computedBonds = bootstrap.getBonds();

		// We print the value of the bonds
		for (int i = 0; i < curveLength; i++) {
			System.out.println("The value of the time " + yearFraction * (i + 1) + " bond is : "
					+ FORMATTERREAL4.format(computedBonds.get(i)));
		}
	}
}
