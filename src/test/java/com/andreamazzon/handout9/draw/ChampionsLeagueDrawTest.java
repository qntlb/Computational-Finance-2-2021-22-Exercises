package com.andreamazzon.handout9.draw;

import org.junit.Test;

/**
 * This class tests the implementation of the program to draw 8 first seeded
 * teams against 8 second seeded teams, with the conditions that they must not
 * be of the same nation and have already played against each other in a
 * previous stage. We can perform hundred of thousands of different draws: if no
 * one give errors, we can be quite confident we are ok.
 *
 *
 * @author Andrea Mazzon
 *
 */
public class ChampionsLeagueDrawTest {

	@Test
	public void drawTest() throws InterruptedException {
		// the exception handling comes from makeDraw()

		// these are the teams of 2021
		FirstSeededTeam realMadrid = new FirstSeededTeam("Real Madrid", "Spain");
		FirstSeededTeam juve = new FirstSeededTeam("Juventus ", "Italy");
		FirstSeededTeam liverpool = new FirstSeededTeam("Liverpool", "England");
		FirstSeededTeam manCity = new FirstSeededTeam("Manchester City", "England");
		FirstSeededTeam manUnited = new FirstSeededTeam("Manchester United", "England");
		FirstSeededTeam bayern = new FirstSeededTeam("Bayern München", "Germany");
		FirstSeededTeam ajax = new FirstSeededTeam("Ajax", "Netherlands");
		FirstSeededTeam lille = new FirstSeededTeam("Lille", "France");

		FirstSeededTeam[] firstSeededTeams = { realMadrid, juve, liverpool, manCity, manUnited, bayern, ajax, lille };

		SecondSeededTeam villareal = new SecondSeededTeam("Villareal", "Spain", manUnited);
		SecondSeededTeam chelsea = new SecondSeededTeam("Chelsea", "England", juve);
		SecondSeededTeam inter = new SecondSeededTeam("Inter Milan", "Italy", realMadrid);
		SecondSeededTeam salzburg = new SecondSeededTeam("RB Salzburg", "Austria", lille);
		SecondSeededTeam sporting = new SecondSeededTeam("Sporting Clube Portugal", "Portugal", ajax);
		SecondSeededTeam benfica = new SecondSeededTeam("Benfica", "Portugal", bayern);
		SecondSeededTeam psg = new SecondSeededTeam("PSG", "France", manCity);
		SecondSeededTeam atletico = new SecondSeededTeam("Atletico Madrid", "Spain", liverpool);

		SecondSeededTeam[] secondSeededTeams = { villareal, chelsea, inter, salzburg, sporting, benfica, psg,
				atletico };

		/*
		 * This is a probably more challenging setting; only for nations, for teams per
		 * nations: much more not allowed matches!
		 */

//		Team realMadrid = new Team("Real Madrid", "Spain");
//		Team barca = new Team("Barcelona ", "Spain");
//		Team liverpool = new Team("Liverpool", "England");
//		Team manCity = new Team("Manchester City ", "England");
//		Team chelsea = new Team("Chelsea ", "England");
//		Team bvb = new Team("Borussia Dortmund", "Germany");
//		Team bayern = new Team("Bayern Munich", "Germany");
//		Team juve = new Team("Juventus", "Italy");
//
//		Team[] firstSeededTeams = { realMadrid, barca, liverpool, manCity, chelsea, bayern, bvb, juve };
//
//		SecondSeededTeam villareal = new SecondSeededTeam("Villareal", "Spain", manCity);
//		SecondSeededTeam sevilla = new SecondSeededTeam("Sevilla ", "Spain", juve);
//		SecondSeededTeam inter = new SecondSeededTeam("Inter Milan", "Italy", realMadrid);
//		SecondSeededTeam atalanta = new SecondSeededTeam("Atalanta Bergamo ", "Italy", barca);
//		SecondSeededTeam roma = new SecondSeededTeam("Roma", "Italy", bvb);
//		SecondSeededTeam united = new SecondSeededTeam("United", "England", bayern);
//		SecondSeededTeam gladbach = new SecondSeededTeam("Gladbach", "Germany", manCity);
//		SecondSeededTeam leipzig = new SecondSeededTeam("RB Leipzig ", "Germany", liverpool);
//
//		SecondSeededTeam[] secondSeededTeams = { villareal, sevilla, inter, atalanta, roma, united, gladbach, leipzig };

		ChampionsLeagueDraw drawer;

		for (int i = 0; i < 1; i++) {
			drawer = new ChampionsLeagueDraw(firstSeededTeams, secondSeededTeams);
			drawer.makeDraw();
		}
	}
}
