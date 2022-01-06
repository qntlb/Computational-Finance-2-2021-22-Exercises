package com.andreamazzon.handout9.draw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * This class performs a whole draw, i.e., 8 first seeded teams are drawn
 * against 8 second seeded teams, with the conditions that they must not be of
 * the same nation and have already played against each other in a previous
 * stage.
 *
 * @author Andrea Mazzon
 *
 */
public class ChampionsLeagueDraw {

	// the lists of the 8 first seeded and the 8 second seeded teams
	private ArrayList<FirstSeededTeam> firstSeededTeams;
	private ArrayList<SecondSeededTeam> secondSeededTeams;

	/*
	 * First it is equal to 8. Then after the first two teams are drawn it's 7, then
	 * 6, on so on.
	 */
	private int numberOfSecondSeededTeamsLeft;

	// the object we need to draw a second seeded team
	private Random selector = new Random();

	/**
	 * It constructs an object to perform a draw.
	 *
	 * @param firstSeededTeams,  an ArrayList representing the eight first seeded
	 *                           teams
	 * @param secondSeededTeams, an ArrayList representing the eight second seeded
	 *                           teams
	 */
	public ChampionsLeagueDraw(ArrayList<FirstSeededTeam> firstSeededTeams,
			ArrayList<SecondSeededTeam> secondSeededTeams) {
		this.firstSeededTeams = firstSeededTeams;
		this.secondSeededTeams = secondSeededTeams;
	}

	/**
	 * It constructs an object to perform a draw.
	 *
	 * @param firstSeededTeams,  an array representing the eight first seeded teams
	 * @param secondSeededTeams, an array representing the eight second seeded teams
	 */
	public ChampionsLeagueDraw(FirstSeededTeam[] firstSeededTeams, SecondSeededTeam[] secondSeededTeams) {
		// we convert arrays to ArrayLists
		this.firstSeededTeams = new ArrayList<>(Arrays.asList(firstSeededTeams));
		this.secondSeededTeams = new ArrayList<>(Arrays.asList(secondSeededTeams));
		numberOfSecondSeededTeamsLeft = 8;
	}

	/*
	 * This method makes a match between one of the remaining second seeded teams
	 * and one of the remaining first seeded teams. As a side effect, it prints the
	 * name of the second seeded team drawn and the names of the first seeded teams
	 * to which it is allowed to be matched.
	 */
	private void makeAMatch() throws InterruptedException {
		System.out.println("Match number " + (8 - numberOfSecondSeededTeamsLeft + 1) + ":");
		System.out.println();

		// a second seeded team is drawn..
		int indexSecondSeeded = selector.nextInt(numberOfSecondSeededTeamsLeft);
		SecondSeededTeam secondSeededTeamDrawn = secondSeededTeams.get(indexSecondSeeded);
		// ..and removed from the list
		secondSeededTeams.remove(secondSeededTeamDrawn);

		System.out.println("Second seeded team drawn: " + secondSeededTeamDrawn.getName());
		System.out.println();

		ArrayList<FirstSeededTeam> allowedOpponents = secondSeededTeamDrawn.getAllowedOpponents(firstSeededTeams,
				secondSeededTeams);

		System.out.println("It can play against:");
		System.out.println();
		// note the foreach syntax
		for (Team allowedOpponent : allowedOpponents) {
			System.out.println(allowedOpponent.getName());
		}
		System.out.println();
		TimeUnit.SECONDS.sleep(3);

		/*
		 * A first seeded team is drawn, among the ones allowed to be matched with the
		 * already drawn second seeded team
		 */
		int indexFirstSeeded = selector.nextInt(allowedOpponents.size());
		Team firstSeededTeamDrawn = allowedOpponents.get(indexFirstSeeded);

		System.out.println("First seeded team drawn: " + firstSeededTeamDrawn.getName());
		System.out.println();
		System.out.println("So the game is: " + secondSeededTeamDrawn.getName() + "-" + firstSeededTeamDrawn.getName());
		System.out.println();
		System.out.println("---------------------------------------");
		System.out.println();
		firstSeededTeams.remove(firstSeededTeamDrawn);

		numberOfSecondSeededTeamsLeft--;
	}

	/**
	 * It makes a whole draw: eight second seeded teams are randomly matched against
	 * eight first seeded team, with the restrictions that they cannot have same
	 * nationality and having being already matched together at a previous stage.
	 *
	 * @throws InterruptedException
	 */
	public void makeDraw() throws InterruptedException {
		for (int matchIndex = 0; matchIndex < 8; matchIndex++) {
			makeAMatch();
			// We pause between one match and the next one
			TimeUnit.SECONDS.sleep(3);
		}
	}
}
