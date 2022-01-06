package com.andreamazzon.handout9.draw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * This class represents a first seeded team which must drawn against a second
 * seeded team. It has only one specific method but it is a very important one:
 * it checks, indeed, if a possible draw would cause a situation where we cannot
 * go ahead with the draw because of the constraints on the nationality of the
 * teams and their previous matches.
 */
public class FirstSeededTeam extends Team {

	/**
	 * It constructs an object representing a first seeded team. Such a team can be
	 * drawn against a second seeded team.
	 *
	 * @param name,   the name of the team
	 * @param nation, the nation of the team
	 */
	public FirstSeededTeam(String name, String nation) {
		super(name, nation);
	}

	/*
	 * This is the most complicated method maybe: we have to make sure that, if we
	 * draw a given second seeded team with a given first seeded team (the one
	 * calling the method) we get a configuration that allows for draws all
	 * respecting the constraints. Therefore, we remove the team represented by the
	 * object calling the method from the list of the remaining first teams and
	 * check.
	 */
	boolean checkIfItMakesNextDrawImpossible(ArrayList<FirstSeededTeam> remainingFirstSeeded,
			ArrayList<SecondSeededTeam> otherSecondSeeded) {

		/*
		 * We copy the list of the remaining first seeded teams and delete the entry of
		 * the object calling the method: note the use of "this"
		 */
		ArrayList<FirstSeededTeam> remainingFirstSeededCopy = new ArrayList<FirstSeededTeam>(remainingFirstSeeded);
		remainingFirstSeededCopy.remove(this);

		/*
		 * listOpponents is an arrayList of arraysLists of FirstSeededTeam: for second
		 * seeded teams, we store the first seeded teams, excluding the one that calls
		 * the method, against which it can be seeded according to the nation and
		 * "already matched" constraints.
		 */
		ArrayList<ArrayList<FirstSeededTeam>> listOpponents = new ArrayList<ArrayList<FirstSeededTeam>>();

		// we create all the entries of listOpponents
		for (SecondSeededTeam secondSeededTeam : otherSecondSeeded) {
			/*
			 * If a second seeded team cannot be matched against any other first seeded
			 * team, excluding the one calling the method, we return true: the configuration
			 * is not possible.
			 */
			ArrayList<FirstSeededTeam> allowedOpponentsForSelectedTeam = secondSeededTeam
					.getAllowedOpponents(remainingFirstSeededCopy);
			if (allowedOpponentsForSelectedTeam.size() == 0) {
				return true;
			}
			listOpponents.add(allowedOpponentsForSelectedTeam);
		}

		/*
		 * What we want to do now is to check if there are n second seeded teams which
		 * share m possible opponents among the remaining first seeded teams after
		 * excluding the one that calls the method, with m<n. "Share" here also covers
		 * situations where not all the teams are the same: for example, we also want to
		 * avoid situations where the second seeded team 1 has possible opponent {A} the
		 * second seeded team 2 has possible opponent {B} and the second seeded team 3
		 * has possible opponents {A, B}. That is, the opponents of n teams are
		 * represented by subsets of a set of size m<n. In order to do that, we first
		 * sort the list of the allowed (up to now) opponents of the second seeded teams
		 * according to the size of the set of opponents. That is, in the previous case
		 * we want {A, B} to be at the top. Then we check if the biggest set has a
		 * number of subsets (including itself) which is bigger than its size. If it is
		 * the case, we return true: the configuration is not possible. Then we do the
		 * same thing for the second biggest set, for the third biggest set and so on.
		 */

		/*
		 * We see here how to sort a list: we can use the (static) sort method of the
		 * class Collections. Such a method needs the list we want to sort and an object
		 * implementing the interface Comparator<ArrayList<FirstSeededTeam>>: this is a
		 * functional interface (i.e., an interface with only one abstract method) with
		 * a method that provides a "rule" to order two elements of type
		 * ArrayList<FirstSeededTeam>. Here we provide this object with an anonymous
		 * class: we give the name of the interface, then "()" like to say we construct
		 * an object of a class implementing this interface, and between brackets we
		 * declare the method we want to override. This method is compare, and accepts
		 * as arguments the elements of type ArrayList<FirstSeededTeam> we want to
		 * compare. The method has to return -1 when the "rank" of the first element has
		 * to be higher that the one of the second element, 1 vice versa, and 0 if they
		 * have same rank. Here this order is based on the length of the list.
		 */
		Collections.sort(listOpponents, new Comparator<ArrayList<FirstSeededTeam>>() {
			@Override
			public int compare(ArrayList<FirstSeededTeam> list1, ArrayList<FirstSeededTeam> list2) {
				// we declare how to order the elements
				if (list1.size() > list2.size()) {
					return -1;
				} else if (list2.size() < list1.size()) {
					return 1;
				}
				return 0;
			}
		});

		// now the list is ordered: we then check for subsets of the biggest sets
		for (int firstTeamIndex = 0; firstTeamIndex < otherSecondSeeded.size() - 1; firstTeamIndex++) {
			// we start by 1: a set is of course a subset of itself
			int countHowManyContained = 1;
			for (int secondTeamIndex = firstTeamIndex + 1; secondTeamIndex < otherSecondSeeded
					.size(); secondTeamIndex++) {
				/*
				 * In order to check if all the elements of a list are contained in another
				 * list, we can use the containsAll method.
				 */
				if (listOpponents.get(firstTeamIndex).containsAll(listOpponents.get(secondTeamIndex))) {
					countHowManyContained++;

				}
			}
			/*
			 * If the number of subsets of the biggest set is strictly bigger than the size
			 * of the biggest set, then it means that n team "share" m possible opponents,
			 * with n>m: this an infeasible configuration, the we return true.
			 */
			if (countHowManyContained > listOpponents.get(firstTeamIndex).size()) {
				return true;
			}
		}

		return false;
	}

}
