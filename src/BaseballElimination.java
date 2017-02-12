import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.LinearProbingHashST;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.StdOut;

public class BaseballElimination
{
	private final int n;
	private final String[] names;
	private final int[] wins;
	private final int[] losses;
	private final int[] remain;
	private final int[][] games;
	private final LinearProbingHashST<String, Integer> nameMap;
	private final Iterable<String>[] cert;

	@SuppressWarnings("unchecked")
	public BaseballElimination(String filename) throws FileNotFoundException // create
																				// a
																				// baseball
																				// division
	// from given filename in format
	// specified below
	{
		Scanner in = new Scanner(new File(filename));
		n = in.nextInt();
		in.nextLine();
		names = new String[n];
		wins = new int[n];
		losses = new int[n];
		remain = new int[n];
		games = new int[n][n];
		nameMap = new LinearProbingHashST<String, Integer>(n << 1);
		cert = new Iterable[n];

		for (int i = 0; i < n; i++)
		{
			names[i] = in.next();
			nameMap.put(names[i], i);
			wins[i] = in.nextInt();
			losses[i] = in.nextInt();
			remain[i] = in.nextInt();
			for (int j = 0; j < n; j++)
			{
				games[i][j] = in.nextInt();
			}
			in.nextLine();
		}
		for (int i = 0; i < n; i++)
		{
			computeTeam(i);
		}
		in.close();
	}

	private void computeTeam(final int team)
	{
		assert team >= 0 && team < n;
		FlowNetwork fn = new FlowNetwork(2 + n + (n - 1) * (n - 2) / 2);
		// artificial source
		int s = 0;
		// sink
		int t = fn.V() - 1;

		// [1, n] is team vertex
		for (int i = 0; i < n; i++)
		{
			if (team == i)
				continue;
			// we prevent team i from winning more than that many games in
			// total, by including an edge from team vertex i to the sink vertex
			// with capacity w[x] + r[x] - w[i].
			int cap = wins[team] + remain[team] - wins[i];
			if (cap < 0)
			{
				Stack<String> certTeam = new Stack<String>();
				certTeam.push(names[i]);
				cert[team] = certTeam;
				return;
			}
			fn.addEdge(new FlowEdge(i + 1, t, cap));
		}

		// [n+1, n(n-1)/2+1] is game vertex
		int gameVertex = n + 1;
		for (int i = 0; i < n; i++)
		{
			if (i == team)
				continue;
			for (int j = i + 1; j < n; j++)
			{
				assert gameVertex != t;
				if (j == team)
					continue;
				fn.addEdge(new FlowEdge(s, gameVertex, games[i][j]));
				fn.addEdge(new FlowEdge(gameVertex, i + 1, games[i][j]));
				fn.addEdge(new FlowEdge(gameVertex, j + 1, games[i][j]));
				gameVertex++;
			}
		}
		Queue<String> certTeams = new Queue<String>();
		FordFulkerson ff = new FordFulkerson(fn, s, t);
		for (int i = 1; i <= n; i++)
		{
			if (ff.inCut(i))
			{
				certTeams.enqueue(names[i - 1]);
			}
		}
		if (certTeams.size() > 0)
			cert[team] = certTeams;
	}

	public int numberOfTeams() // number of teams
	{
		return n;
	}

	public Iterable<String> teams() // all teams
	{
		ArrayList<String> teams = new ArrayList<String>(n);
		for (String s : names)
			teams.add(s);
		return teams;
	}

	public int wins(String team) // number of wins for given team
	{
		Integer t = nameMap.get(team);
		if (t == null)
			throw new IllegalArgumentException();
		return wins[t];
	}

	public int losses(String team) // number of losses for given team
	{
		Integer t = nameMap.get(team);
		if (t == null)
			throw new IllegalArgumentException();
		return losses[t];
	}

	public int remaining(String team) // number of remaining games for given
										// team
	{
		Integer t = nameMap.get(team);
		if (t == null)
			throw new IllegalArgumentException();
		return remain[t];
	}

	public int against(String team1, String team2) // number of remaining games
													// between team1 and team2
	{
		Integer t1 = nameMap.get(team1);
		Integer t2 = nameMap.get(team2);
		if (t1 == null || t2 == null)
			throw new IllegalArgumentException();
		return games[t1][t2];
	}

	public boolean isEliminated(String team) // is given team eliminated?
	{
		Integer t = nameMap.get(team);
		if (t == null)
			throw new IllegalArgumentException();
		return cert[t] != null;
	}

	public Iterable<String> certificateOfElimination(String team) // subset R of
																	// teams
																	// that
																	// eliminates
																	// given
																	// team;
																	// null if
																	// not
																	// eliminated
	{
		Integer t = nameMap.get(team);
		if (t == null)
			throw new IllegalArgumentException();
		return cert[t];
	}

	public static void main(String[] args) throws FileNotFoundException
	{
		BaseballElimination division = new BaseballElimination(args[0]);
		for (String team : division.teams())
		{
			if (division.isEliminated(team))
			{
				StdOut.print(team + " is eliminated by the subset R = { ");
				for (String t : division.certificateOfElimination(team))
				{
					StdOut.print(t + " ");
				}
				StdOut.println("}");
			} else
			{
				StdOut.println(team + " is not eliminated");
			}
		}
	}
}
