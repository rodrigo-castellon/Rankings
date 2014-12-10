import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.io.*;

import org.jgrapht.alg.CycleDetector;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
public class rankingsMain 
{
	private final static String fileName = "rankings2.txt";
	private final static int distanceBetweenTeams = 4;

	public static void main(String[] args) 
	{
		Map<String, Object>fileResponses = readFile();
		DirectedGraph graph = (DirectedGraph) fileResponses.get("customGraph");
		SimpleDirectedGraph<String, DefaultEdge> jgraph = (SimpleDirectedGraph<String, DefaultEdge>) fileResponses.get("jgraph");
		CycleDetector cycleFinder = new CycleDetector(jgraph);
		graph.adjustForCycles(cycleFinder);
		Map<String, Integer> totalScore = adjustTotalsForWins(graph, (HashMap<String, Integer>) fileResponses.get("score"));
		List<String> sortedGraph = graph.topSort();
		System.out.println(sortedGraph);
		sortedGraph = fixTies(sortedGraph, (Map<String, Integer>)fileResponses.get("wins"), totalScore);
		System.out.println(sortedGraph);
		
		
		
	}

	
	public static Map<String, Integer>adjustTotalsForWins(DirectedGraph graph, HashMap<String, Integer> totalScore)
	{
		for (String team : graph.getVerticies())
		{
			int scoreLoser = totalScore.get(team);
			for (String beat : graph.getConnections(team))
			{
				int scoreWinner = totalScore.get(beat);
				if (scoreLoser > scoreWinner)
				{
					totalScore.put(beat, scoreLoser + 1);
				}
			}
		}
		return totalScore;
	}
		
	public static ArrayList<String> fixTies (List<String> topSorted, Map<String, Integer> winCount, Map<String, Integer> totalScore)
	{
		ArrayList<String> finalList = new ArrayList<String>();
		Iterator<String> it = topSorted.iterator();
		String next = it.next();
		while (it.hasNext())
		{
			String firstRank = next;
			int numWins = winCount.get(firstRank);
			ArrayList<String> group = new ArrayList<String>();
			group.add(firstRank);
			next = it.next();
			while (winCount.get(next) == numWins)
			{
				group.add(next);
				if(it.hasNext())
				{
					next = it.next();
				}
				else
				{
					break;
				}
				
			}
			if (group.size() > 1)
			{
				group = innerRank(group, totalScore);
			}
			finalList = addArrayContents(finalList, group);
			
		}
		return finalList;
	
	}
	public static ArrayList<String> innerRank(ArrayList<String> teams, Map<String, Integer>totalScore)
	{
		ArrayList<teamNode> toSort = new ArrayList<teamNode>();
		for (String team : teams)
		{
			teamNode node = new teamNode(team, totalScore.get(team));
			toSort.add(node);
		}
		Collections.sort(toSort);
		Collections.reverse(toSort);
		ArrayList<String> toReturn = new ArrayList<String>();
		for (teamNode node : toSort)
		{
			toReturn.add(node.getTeamName());
		}
		return toReturn;
	}
	
	public static void sort(List<String> toSort)
	{
		
	}
	
	public static ArrayList<String> addArrayContents(ArrayList<String> firstArray, ArrayList<String> secondArray)
	{
		for (String x: secondArray)
		{
			firstArray.add(x);
		}
		return firstArray;
	}
	
	public static Map<String, Object> readFile()
	{
		Scanner s;
		DirectedGraph graph = new DirectedGraph();
		SimpleDirectedGraph<String, DefaultEdge> jgraph = new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		HashMap <String, Integer> totalScoreMap = new HashMap<String, Integer>();
		HashMap <String, Integer> winCount = new HashMap<String, Integer>();
		try 
		{
			s = new Scanner(new BufferedReader(new FileReader(fileName)));
			while(s.hasNext())
			{
				String line = s.nextLine();
				String linePairTwo = s.nextLine();
				int indexOfSpace = linePairTwo.indexOf(" ");
				int firstNumber = Integer.parseInt(linePairTwo.substring(0, indexOfSpace));
				int secondNumber = Integer.parseInt(linePairTwo.substring(indexOfSpace + 1));
				indexOfSpace = line.indexOf(" ");
				String teamOne = line.substring(0, indexOfSpace);
				String teamTwo = line.substring(indexOfSpace + distanceBetweenTeams);
				if (graph.containsVertex(teamOne) == false)
				{
					totalScoreMap.put(teamOne, 0);
					winCount.put(teamOne, 0);
					graph.addVertex(teamOne);
					jgraph.addVertex(teamOne);
				}
				if (graph.containsVertex(teamTwo) == false)
				{
					totalScoreMap.put(teamTwo, 0);
					winCount.put(teamTwo, 0);
					graph.addVertex(teamTwo);
					jgraph.addVertex(teamTwo);
				}

				if(firstNumber > secondNumber)
				{
					int currentCount = winCount.get(teamOne);
					winCount.put(teamOne, currentCount + 1);
					graph.addEdge(teamTwo, teamOne);
					jgraph.addEdge(teamTwo, teamOne);
				}
				else if (firstNumber < secondNumber)
				{
					int currentCount = winCount.get(teamTwo);
					winCount.put(teamTwo, currentCount + 1);
					graph.addEdge(teamOne, teamTwo);
					jgraph.addEdge(teamOne, teamTwo);
				}
				else
				{
					//Special Case are equal
					System.out.println("Special Case Activated. Tie game.");
				}			
				int currentPointCount = totalScoreMap.get(teamOne);
				totalScoreMap.put(teamOne, currentPointCount + firstNumber);
				currentPointCount = totalScoreMap.get(teamTwo);
				totalScoreMap.put(teamTwo, currentPointCount + secondNumber);
				
			}

			s.close();
			Map<String, Object> toReturn = new HashMap<String, Object>();
			toReturn.put("customGraph", graph);
			toReturn.put("jgraph", jgraph);
			toReturn.put("wins", winCount);
			toReturn.put("score", totalScoreMap);
			return toReturn;
		} 
		catch (FileNotFoundException e) 
		{
			System.out.println("File not found");
			return null;
		} 




	}
}

class teamNode implements Comparable
{
	String teamName;
	int totalScore;
	
	public teamNode(String name, int totalScore)
	{
		this.teamName = name;
		this.totalScore = totalScore;
	}
	
	public String getTeamName()
	{
		return teamName;
	}
	
	public int getTotalScore()
	{
		return totalScore;
	}

	public int compareTo(Object o) 
	{
		return this.totalScore - ((teamNode) o).getTotalScore();
	}
}




