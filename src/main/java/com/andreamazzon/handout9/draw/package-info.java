/**
 * In this package we provide the implementation of a program in order to
 * randomly match eight first seeded teams against eight the second seeded
 * teams. Rules are: • Teams of the same nationality cannot play against each
 * other. • Every first seeded team has already played in a former stage against
 * a second seeded team (i.e., they have already been matched): these teams
 * cannot play against each other. The main problem is the following: since we
 * don’t want to have to make a second draw, we have to avoid to end up in
 * situations where you have no possible choice. For example, we must avoid
 * situations where: • At least one second seeded team cannot play against any
 * first seeded team, because of the nation and/or the "having being already
 * matched in a previous stage" rules. • At least two teams, both first seeded
 * or both second seeded, have only one possible choice, and is the same. • The
 * second seeded team 1 has possible opponent {A} the second seeded team 2 has
 * possible opponent {B} and the second seeded team 3 has possible opponents
 * {A,B}. The code here is organized as follows: the draw itself is performed in
 * the class ChampionsLeagueDraw, which has a private method which matches two
 * teams and a public method that, basing on the latter, matches the eight
 * second seeded teams against the eight top seeded teams. Top seeded and second
 * seeded teams are represented by the classes FirstSeededTeam and
 * SecondSeededTeam, respectively, which both inherit from the class Team. The
 * class SecondSeededTeam has methods to check the top seeded opponents it can
 * be matched sticking to the two constraints listed above. The class
 * FirstSeededTeam has a method which checks if, in the case when a given second
 * seeded team is matched against the top seeded team calling the method, the
 * remaining teams form a feasible configuration, i.e., if it is still possible
 * to draw all these teams following the rules above. The main issues here are
 * tackled using ArrayLists: all this code can be seen as an exercise about how
 * to use the functionality of ArrayLists.
 */
package com.andreamazzon.handout9.draw;