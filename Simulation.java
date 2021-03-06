/*
 * Class that defines the simulation environment.
 * 
 * Written by James P. Biagioni (jbiagi1@uic.edu)
 * for CS511 Artificial Intelligence II
 * at The University of Illinois at Chicago
 * 
 * Last modified 3/5/07 
 * 
 * DISCLAIMER:
 * Elements of this application were borrowed from
 * the client-server implementation of the Wumpus
 * World Simulator written by Kruti Mehta at
 * The University of Texas at Arlington.
 * 
 */

import java.io.BufferedWriter;

class Simulation {
	
	private int currScore = 0;
	private static int actionCost = -1;
	private static int deathCost = -1000;
	private static int shootCost = -10;
	private int stepCounter = 0;
	private int lastAction = 0;
	
	private boolean simulationRunning;
	
	private Agent agent;
	private Environment environment;
	private TransferPercept transferPercept;
	private BufferedWriter outputWriter;
	
	private boolean PRINT_ENABLE = AgentFunction.debugMode;
	
	public Simulation(Environment wumpusEnvironment, int maxSteps, BufferedWriter outWriter, boolean nonDeterministic) {
		
		// start the simulator
		simulationRunning = true;
		
		outputWriter = outWriter;
		transferPercept = new TransferPercept(wumpusEnvironment);
		environment = wumpusEnvironment;
		
		agent = new Agent(environment, transferPercept, nonDeterministic);
		
		environment.placeAgent(agent);
		
		if(PRINT_ENABLE)
			environment.printEnvironment();
		
		printCurrentPerceptSequence();
		
		try {
			if(PRINT_ENABLE)
				System.out.println("Current score: " + currScore);
			outputWriter.write("Current score: " + currScore + "\n");
			
			while (simulationRunning == true && stepCounter < maxSteps) {
				if(PRINT_ENABLE)
					System.out.println("Last action: " + Action.printAction(lastAction));
				outputWriter.write("Last action: " + Action.printAction(lastAction) + "\n");
				
				if(PRINT_ENABLE)
					System.out.println("Time step: " + stepCounter);
				outputWriter.write("Time step: " + stepCounter + "\n");
				
				handleAction(agent.chooseAction());
				wumpusEnvironment.placeAgent(agent);
				if(PRINT_ENABLE)
					environment.printEnvironment();								
				printCurrentPerceptSequence();
				
				if(PRINT_ENABLE)
					System.out.println("Current score: " + currScore);
				outputWriter.write("Current score: " + currScore + "\n");
				
				//Scanner in = new Scanner(System.in);
				//in.next();
				
				stepCounter += 1;
				
				if (stepCounter == maxSteps || simulationRunning == false) {
					if(PRINT_ENABLE)
						System.out.println("Last action: " + Action.printAction(lastAction));
					outputWriter.write("Last action: " + Action.printAction(lastAction) + "\n");
					
					if(PRINT_ENABLE)
						System.out.println("Time step: " + stepCounter);
					outputWriter.write("Time step: " + stepCounter + "\n");
					
					lastAction = Action.END_TRIAL;
				}
				
				if (agent.getHasGold() == true) {
					if(PRINT_ENABLE)
						System.out.println("\n" + agent.getName() + " found the GOLD!!");
					outputWriter.write("\n" + agent.getName() + " found the GOLD!!\n");
				}
				if (agent.getIsDead() == true) {
					if(PRINT_ENABLE)
						System.out.println("\n" + agent.getName() + " is DEAD!!");
					outputWriter.write("\n" + agent.getName() + " is DEAD!!\n");	
				}
				
			}
			
		}
		catch (Exception e) {
			System.out.println("An exception was thrown: " + e);
		}		
		
		printEndWorld();
		
	}
	
	public void printEndWorld() {
		
		try {
			if(PRINT_ENABLE)
				environment.printEnvironment();
			if(PRINT_ENABLE)
				System.out.println("Final score: " + currScore);
			outputWriter.write("Final score: " + currScore + "\n");
			if(PRINT_ENABLE)
				System.out.println("Last action: " + Action.printAction(lastAction));
			outputWriter.write("Last action: " + Action.printAction(lastAction) + "\n");

		}
		catch (Exception e) {
			System.out.println("An exception was thrown: " + e);
		}
		
	}
	
	public void printCurrentPerceptSequence() {
		
		try {
			if(PRINT_ENABLE)
				System.out.print("Percept: <");	
			outputWriter.write("Percept: <");
			
			if (transferPercept.getBump() == true) {
				if(PRINT_ENABLE)
					System.out.print("bump,");
				outputWriter.write("bump,");
			}
			else if (transferPercept.getBump() == false) {
				if(PRINT_ENABLE)
					System.out.print("none,");
				outputWriter.write("none,");
			}
			if (transferPercept.getGlitter() == true) {
				if(PRINT_ENABLE)
					System.out.print("glitter,");
				outputWriter.write("glitter,");
			}
			else if (transferPercept.getGlitter() == false) {
				if(PRINT_ENABLE)
					System.out.print("none,");
				outputWriter.write("none,");
			}
			if (transferPercept.getBreeze() == true) {
				if(PRINT_ENABLE)
					System.out.print("breeze,");
				outputWriter.write("breeze,");
			}
			else if (transferPercept.getBreeze() == false) {
				if(PRINT_ENABLE)
					System.out.print("none,");
				outputWriter.write("none,");
			}
			if (transferPercept.getStench() == true) {
				if(PRINT_ENABLE)
					System.out.print("stench,");
				outputWriter.write("stench,");
			}
			else if (transferPercept.getStench() == false) {
				if(PRINT_ENABLE)
					System.out.print("none,");
				outputWriter.write("none,");
			}
			if (transferPercept.getScream() == true) {
				if(PRINT_ENABLE)
					System.out.print("scream>\n");
				outputWriter.write("scream>\n");
			}
			else if (transferPercept.getScream() == false) {
				if(PRINT_ENABLE)
					System.out.print("none>\n");
				outputWriter.write("none>\n");
			}
		
		}
		catch (Exception e) {
			System.out.println("An exception was thrown: " + e);
		}
		
	}
	
	public void handleAction(int action) {
		
		try {
		
			if (action == Action.GO_FORWARD) {
				
				if (environment.getBump() == true) environment.setBump(false);
				
				agent.goForward();
				environment.placeAgent(agent);
				
				if (environment.checkDeath() == true) {
					
					currScore += deathCost;
					simulationRunning = false;
					
					agent.setIsDead(true);
				}
				else {
					currScore += actionCost;
				}
				
				if (environment.getScream() == true) environment.setScream(false);
				
				lastAction = Action.GO_FORWARD;
			}
			else if (action == Action.TURN_RIGHT) {
				
				currScore += actionCost;
				agent.turnRight();		
				environment.placeAgent(agent);
				
				if (environment.getBump() == true) environment.setBump(false);
				if (environment.getScream() == true) environment.setScream(false);
				
				lastAction = Action.TURN_RIGHT;
			}
			else if (action == Action.TURN_LEFT) {
				
				currScore += actionCost;
				agent.turnLeft();		
				environment.placeAgent(agent);
				
				if (environment.getBump() == true) environment.setBump(false);
				if (environment.getScream() == true) environment.setScream(false);
				
				lastAction = Action.TURN_LEFT;
			}
			else if (action == Action.GRAB) {
				
				if (environment.grabGold() == true) {
					
					currScore += 1000;
					simulationRunning = false;
					
					agent.setHasGold(true);
				}
				else currScore += actionCost;
				
				environment.placeAgent(agent);
				
				if (environment.getBump() == true) environment.setBump(false);
				if (environment.getScream() == true) environment.setScream(false);
				
				lastAction = Action.GRAB;
			}
			else if (action == Action.SHOOT) {
				
				if (agent.shootArrow() == true) {
					
					if (environment.shootArrow() == true) environment.setScream(true);
				
					currScore += shootCost;					
				}
				else {
					
					if (environment.getScream() == true) environment.setScream(false);
					
					currScore += actionCost;
				}
				
				environment.placeAgent(agent);
				
				if (environment.getBump() == true) environment.setBump(false);
				
				lastAction = Action.SHOOT;
			}
			else if (action == Action.NO_OP) {
				
				environment.placeAgent(agent);
				
				if (environment.getBump() == true) environment.setBump(false);
				if (environment.getScream() == true) environment.setScream(false);
				
				lastAction = Action.NO_OP;
			}
			
		}
		catch (Exception e) {
			System.out.println("An exception was thrown: " + e);
		}
	}
	
	public int getScore() {
		
		return currScore;
		
	}
	
}