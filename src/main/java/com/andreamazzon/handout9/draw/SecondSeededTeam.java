package com.andreamazzon.handout9.draw;

import java.util.ArrayList;

/**
 * This class represents a second seeded team which must drawn against a first
 * seeded team. It has four specific methods: one is public (an overloaded
 * version of a private one, in the sense that has same name but different
 * arguments list) and two are private and one has package access. At every step
 * of the draw, one second team is drawn: the public method is called by the
 * object representing the drawn team, in order to check against which of the
 * remaining first seeded teams it be matched, respecting the constraints. Such
 * a public method uses and calls the private methods.
 *
 * @author Andrea Mazzon
 *
 */
public class SecondSeededTeam extends Team {

	FirstSeededTeam formerOpponent;

	/**
	 * It constructs an object representing a second seeded team. Such a team can be
	 * drawn against a first seeded team.
	 *
	 * @param name,   the name of the team
	 * @param nation, the nation of the team
	 */
	public SecondSeededTeam(String name, String nation, FirstSeededTeam formerOpponent) {
		super(name, nation);
		this.formerOpponent = formerOpponent;
	}

	/*
	 * This method checks if the first seeded team given as an argument is of the
	 * same nation as the team represented by the object calling the method.
	 */
	private boolean checkIfSameNation(FirstSeededTeam possibleOpponent) {
		// note how to compare two strings
		return nation.contentEquals(possibleOpponent.getNation());
	}

	/*
	 * This method checks if the first seeded team given as an arguments has already
	 * been matched in a previous stage against the team represented by the object
	 * calling the method.
	 */
	private boolean checkIfFormerOpponent(FirstSeededTeam possibleOpponent) {
		// note how to compare two strings
		return (formerOpponent.getName().contentEquals(possibleOpponent.getName()));
	}

	/**
	 * It gives a list of first seeded teams against which the second seeded team
	 * represented by the object calling the method can be matched, respecting the
	 * constraints and making it possible to go ahead with the draw respecting the
	 * constraints.
	 *
	 * @param remainingFirstSeededTeams, a List representing objects of type
	 *                                   FirstSeededTeam: it represents the
	 *                                   remaining first seeded teams after the
	 *                                   previous steps of the draw. From this list,
	 *                                   the teams against which the second seeded
	 *                                   team calling the method can be matched are
	 *                                   selected
	 * @param otherSecondSeededTeams,    a List representing objects of type
	 *                                   SecondSeededTeam: it represents the
	 *                                   remaining second seeded teams after the
	 *                                   previous steps of the draw, except
	 *                                   therefore the team calling the method. They
	 *                                   are needed in order to check if the
	 *                                   configuration following a given draw is
	 *                                   given, i.e., if it makes it possible to
	 *                                   find all matches respecting the
	 *                                   constraints.
	 * @return a list of first seeded teams against which the second seeded team
	 *         represented by the object calling the method can be matched,
	 *         respecting the constraints and making it possible to go ahead with
	 *         the draw respecting the constraints.
	 */
	public ArrayList<FirstSeededTeam> getAllowedOpponents(ArrayList<FirstSeededTeam> remainingFirstSeededTeams,
			ArrayList<SecondSeededTeam> otherSecondSeededTeams) {
		/*
		 * We copy the list of the remaining first seeded teams: remember that we cannot
		 * say remainingFirstSeededCopy=remainingFirstSeededTeams, because in this case
		 * they share the same reference, i.e., they are the same object. We copy it
		 * because we want to make the modifications to this copy, checking instead all
		 * the teams from the original list. Basically we will delete teams against
		 * which the team calling the method cannot be drawn. We will then return this
		 * copy.
		 */
		ArrayList<FirstSeededTeam> remainingFirstSeededCopy = new ArrayList<FirstSeededTeam>(remainingFirstSeededTeams);

		/*
		 * We check every possible first seeded teams in order to see if it respects the
		 * constraints: note the use of the foreach syntax.
		 */
		for (FirstSeededTeam possibleOpponent : remainingFirstSeededTeams) {
			if (checkIfSameNation(possibleOpponent) || checkIfFormerOpponent(possibleOpponent) || possibleOpponent
					/*
					 * We give the list of the second seeded teams from which the second seeded team
					 * currently drawn (i.e., the one represented by the object calling the method)
					 * has already been removed. The method here below, called by possibleOpponent,
					 * will check if the configuration given by these second seeded teams and the
					 * first seeded teams without possibleOpponent is "feasible" i.e., if it allows
					 * draws all respecting the constraints.
					 */
					.checkIfItMakesNextDrawImpossible(remainingFirstSeededTeams, otherSecondSeededTeams)) {
				/*
				 * If it does not respect the constraints, or if it gives rise to a
				 * configuration where we could not go ahead, we remove it from the copy of the
				 * list. Removing it from the original list when checking the original list
				 * itself would be dangerous.
				 */
				remainingFirstSeededCopy.remove(possibleOpponent);
			}
		}
		return remainingFirstSeededCopy;
	}

	/*
	 * This method has package access: it can only be called from inside a method
	 * written in classes of this package: this is the case in the method
	 * checkIfItMakesNextDrawImpossible of FirstSeededTeam. It checks which ones of
	 * the remaining first seeded teams "respect the constraints" in the sense that
	 * they have not same nation as the team calling the method and have not been
	 * matched against it in the previous stage. That is, here we don't care about
	 * the configuration coming from this possible draw.
	 */
	ArrayList<FirstSeededTeam> getAllowedOpponents(ArrayList<FirstSeededTeam> remainingFirstSeeded) {
		ArrayList<FirstSeededTeam> remainingFirstSeededCopy = new ArrayList<FirstSeededTeam>(remainingFirstSeeded);
		for (FirstSeededTeam possibleOpponent : remainingFirstSeeded) {
			if (checkIfSameNation(possibleOpponent) || checkIfFormerOpponent(possibleOpponent)) {
				remainingFirstSeededCopy.remove(possibleOpponent);
			}
		}
		return remainingFirstSeededCopy;
	}

}
