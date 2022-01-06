package com.andreamazzon.handout9.draw;

/**
 * This is class representing a team that has to be drawn. It can be first
 * seeded or second seeded. Remember that first seeded teams can only play
 * against second seeded teams, and viceversa. It will be inherited by two
 * classes representing first seeded and second seeded teams, which have some
 * specific methods.
 *
 * @author Andrea Mazzon
 *
 */
public class Team {

	private String name;
	protected String nation;

	/**
	 * It constructs an object representing a team.
	 *
	 * @param name,   the name of the team
	 * @param nation, the nation of the team
	 */
	public Team(String name, String nation) {
		this.name = name;
		this.nation = nation;
	}

	/**
	 * It gets the nation of the team
	 *
	 * @return the nation of the team
	 */
	public String getNation() {
		return nation;
	}

	/**
	 * It gets the name of the team
	 *
	 * @return the name of the team
	 */
	public String getName() {
		return name;
	}
}
